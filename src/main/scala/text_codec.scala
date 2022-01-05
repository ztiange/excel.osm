package osm.tuplelike.text

import osm.tuplelike._
import osm.tuplelike.types._

import java.util.Date

trait Codec[T] extends osm.tuplelike.Codec[T, String]

given Codec[Int] with {
  def encode(v: Int) = (_) => v.toString
  def decode(v: String) = tryNel { v.toInt }
}
given Codec[Float] with {
  def encode(v: Float) = (_) => v.toString
  def decode(v: String) = tryNel { v.toFloat }
}
given Codec[Double] with {
  def encode(v: Double) = (_) => v.toString
  def decode(v: String) = tryNel { v.toDouble }
}
given Codec[String] with {
  def encode(v: String) = (_) => v
  def decode(v: String) = tryNel { v }
}

given (using formatter: DateTimeFormat): Codec[DateTime] with {
  def encode(v: DateTime) = (_) =>
    formatter.format(v)
  def decode(v: String) = tryNel {
    DateTime(formatter.parse(v))
  }
}

given (using formatter: DateFormat): Codec[Date] with {
  def encode(v: Date) = (_) => formatter.format(v)
  def decode(v: String) = tryNel {
    formatter.parse(v)
  }
}
