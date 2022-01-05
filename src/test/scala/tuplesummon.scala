package osm.tuplelike.test

import org.junit.Test
import org.junit.Assert.*
import osm.tuplelike.Codec
import cats.implicits._
import cats.data._
import cats.data.Validated._

import osm.tuplelike._

object givenInstances{

    given Codec[String,String] with
      def encode(t: String) = _ => t
      def decode(s: String) = s.validNel

    given Codec[Int,String] with
      def encode(t: Int) = _ => t.toString
      def decode(s: String) = Integer.parseInt(s).validNel


    given Conversion[String, TupleLike[String]] with
      def apply(r: String) : TupleLike[String] = new TupleLike[String](){
        def length() : Int = r.split(",").length

        def components() : Seq[String] = r.split(",").toSeq
      }
}

class TupleSummon:
  @Test def decoderTest(): Unit = {
    import givenInstances.{given}
    case class Person(name: String, age: Int)
    assertEquals(
      Decoder[Person,String]("Tom,10"),
      Person("Tom",10).validNel
    )
    assertEquals(
      Decoder[Person,String]("Tom"),
      "行长度不足，需要2项，只有1项".invalidNel
    )
  }

  @Test def encoderTest(): Unit = {
    import givenInstances.{given}
    case class Person(name: String, age: Int)
    val encode = Encoder[Person,String](Person("Tom",10)).map(_(""))
    assertEquals(encode, List("Tom","10"))

    

  }
    
