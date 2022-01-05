package osm.tuplelike

import scala.deriving.Mirror
import scala.compiletime.{erasedValue, summonInline, summonAll}
import collection.JavaConverters._
import cats.implicits._
import cats.data._
import cats.data.Validated._
import java.util.Date
import cats.syntax.try_

def tryNel[A](f: => A): ValidatedValue[A] = {
  catchNonFatal { f }.leftMap(x => NonEmptyList.one(x.getMessage))
}

inline def cast[T](f: Any): ValidatedValue[T] =
  catchNonFatal { f.asInstanceOf[T] }.leftMap(ee =>
    NonEmptyList.one(s"Cast Error: ${ee.getMessage}")
  )

type ValidatedValue[T] = ValidatedNel[String, T]

/**
 * Codec为一个“外部的数据结构”与Scala的类型之间相互转换的typeclass。
 * 典型的“外部数据结构”有 csv 或者 excel 中的一条数据或者一行数据
 * 
 * 对于csv有 type CsvCodec[S] = Codec[S,String]
 * 而对于Excel有 type ExcelCodec[T] = Codec[T,Cell]
 * 
 * 在这里假设从Scala的数据T变换为外部数据S时，只要编译通过便不会发生错误，而反方向作为输入，可能会发生格式解析的错误。
 */
trait Codec[S, T] {
  def encode(v: S): (T) => T
  def decode(v: T): ValidatedValue[S]
}

inline def summonAllCodec[S <: Tuple, TGT]: List[Codec[Any, TGT]] =
    inline erasedValue[S] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) =>
        summonInline[Codec[t, TGT]].asInstanceOf[Codec[Any, TGT]] :: summonAllCodec[ts, TGT]
    end match
end summonAllCodec

/**
 * “外部的”一个可以转换为Tuple的数据
 * 比如csv中的一行为TupleLike[String], excel中的一行为TupleLike[Row]
 */
trait TupleLike[T]{
  def length():Int
  def components():Seq[T]
}

inline def Decoder[S,T](using
    m: Mirror.ProductOf[S]
): TupleLike[T] => ValidatedValue[S] =
  record => {
    lazy val vs = summonAllCodec[m.MirroredElemTypes, T]
    Decoder_T[S,T](vs)(record)
  }

def Decoder_T[S,T](using
    m: Mirror.ProductOf[S]
)(vs: List[Codec[Any, T]]): TupleLike[T] => ValidatedValue[S] =
  record => {
    if (vs.length > record.length())
      (s"行长度不足，需要${vs.length}项，只有${record.length()}项").invalidNel
    else
      vs.zip(record.components())
        .map((codec, strv) =>
          codec
            .decode(strv)
            .leftMap(x => x.map(msg => s"${strv}:${msg}"))
        )
        .sequence
        .andThen(values =>
          tryNel { m.fromProduct(Tuple.fromArray(values.toArray)) }
        )
  }  


inline def Encoder[S <: Product, T](using
    m: Mirror.ProductOf[S]
): S => List[(T) => T] = { (value: S) =>
  {
    val vs = summonAllCodec[m.MirroredElemTypes, T]
    Encoder_T[S,T](vs)(value)
  }
}

def Encoder_T[T <: Product, S](using
    m: Mirror.ProductOf[T]
)(vs: List[Codec[Any, S]]): T => List[(S) => S] = { (value: T) =>
  {
    vs.zip(value.productIterator.toList).map((codec, v) => codec.encode(v))
  }
}


