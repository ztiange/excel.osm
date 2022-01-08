package osm.tuplelike

import collection.JavaConverters._
import cats.implicits._
import cats.data._
import cats.data.Validated._


def tryNel[A](f: => A): ValidatedValue[A] = {
  catchNonFatal { f }.leftMap(x => NonEmptyList.one(x.getMessage))
}

inline def cast[T](f: Any): ValidatedValue[T] =
  catchNonFatal { f.asInstanceOf[T] }.leftMap(ee =>
    NonEmptyList.one(s"Cast Error: ${ee.getMessage}")
  )

type ValidatedValue[T] = ValidatedNel[String, T]