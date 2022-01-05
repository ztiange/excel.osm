package osm.tuplelike.excel

import osm.tuplelike._

import java.util.Date
import java.text.SimpleDateFormat

import scala.deriving.Mirror
import scala.compiletime.{erasedValue, summonInline, summonAll}
import collection.JavaConverters._

import cats.implicits._
import cats.data._
import cats.data.Validated._
import cats.syntax.try_


import osm.tuplelike.text.{given}
import osm.tuplelike._
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row


trait Codec[T] extends osm.tuplelike.Codec[T, Cell]
object Codec{
  def apply[T:Codec]() = summon[Codec[T]]
}

def isEmpty(c: Cell): Boolean = {
  c == null || c.getCellType == CellType.BLANK
}


def blankCheck[T](v: Cell): ValidatedValue[Cell] =
  condNel(
    !isEmpty(v),
    v,
    "unexpected empty cell"
  )

def strBlankCheck[T](v: Cell): ValidatedValue[Cell] =
  condNel(
    v != null && v.getCellType != CellType.BLANK && !v.getStringCellValue.trim.isEmpty,
    v,
    "unexpected empty cell"
  )

def update(f: (Cell) => Unit): (Cell) => Cell = c => {
  f(c)
  c
}

given Codec[String] with {
  def encode(v: String) = update(_.setCellValue(v))
  def decode(v: Cell) = blankCheck[String](v).andThen { x =>
    tryNel { x.getStringCellValue }
  }
}
given Codec[Int] with {
  def encode(v: Int) = update(_.setCellValue(v))
  def decode(v: Cell) = tryNel { v.getNumericCellValue.toInt }
    .orElse {
      summon[osm.tuplelike.text.Codec[Int]].decode(v.getStringCellValue)
    }
}

given Codec[Float] with {
  def encode(v: Float) = update(_.setCellValue(v))
  def decode(v: Cell) = tryNel { v.getNumericCellValue.toFloat }
    .orElse {
      summon[osm.tuplelike.text.Codec[Float]].decode(v.getStringCellValue)
    }
}

given Codec[Double] with {
  def encode(v: Double) = update(_.setCellValue(v))
  def decode(v: Cell) = tryNel { v.getNumericCellValue.toDouble }
    .orElse {
      summon[osm.tuplelike.text.Codec[Double]].decode(v.getStringCellValue)
    }
}

given datecodec(using strcoded: osm.tuplelike.text.Codec[Date]): Codec[Date] with {
  def encode(v: Date) = update(_.setCellValue(v))
  def decode(v: Cell) = {
    blankCheck[Date](v)
      .andThen { x => tryNel { x.getDateCellValue } }
      .orElse(strcoded.decode(v.getStringCellValue))
  }
}

//Codec[Option[T]]
given [T: Codec]: Codec[Option[T]] with {
  def encode(v: Option[T]) = {
    v match {
      case None    => update(_.setBlank)
      case Some(d) => summon[Codec[T]].encode(d)
    }
  }
  def decode(v: Cell) = {
    if (!isEmpty(v))
      summon[Codec[T]].decode(v).map(_.some)
    else None.valid
  }
}

inline given [S <: Product](using
    m: Mirror.ProductOf[S]
): osm.tuplelike.Codec[S, Row] = new osm.tuplelike.Codec[S, Row]() {
  def encode(v: S) = {
    val encodeCells = Encoder[S,Cell](v)
    row => {
      row.cells.padZip(encodeCells).zipWithIndex.map {
        case ((cell, value), index) =>
          value.map(_(cell.getOrElse(row.createCell(index))))
      }
      row
    }
  }
  def decode(record: Row) = {
    Decoder[S,Cell](record)
  }
}




