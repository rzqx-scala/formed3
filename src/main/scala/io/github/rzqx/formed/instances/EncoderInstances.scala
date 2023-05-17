package io.github.rzqx.formed.instances

import io.github.rzqx.formed.FormEncoder

import cats.*
import cats.data.Chain
import cats.implicits.*

import scala.deriving.Mirror

import java.util.UUID

trait EncoderInstances:
  implicit val encodeString: FormEncoder[String] = (v: String) => Chain(Chain.empty -> v)
  implicit val encodeUnit: FormEncoder[Unit] = (_: Unit) => Chain.empty
  implicit val encodeBoolean: FormEncoder[Boolean] = encodeString.contramap(_.toString)
  implicit val encodeByte: FormEncoder[Byte] = encodeString.contramap(_.toString)
  implicit val encodeShort: FormEncoder[Short] = encodeString.contramap(_.toString)
  implicit val encodeInt: FormEncoder[Int] = encodeString.contramap(_.toString)
  implicit val encodeLong: FormEncoder[Long] = encodeString.contramap(_.toString)
  implicit val encodeFloat: FormEncoder[Float] = encodeString.contramap(_.toString)
  implicit val encodeDouble: FormEncoder[Double] = encodeString.contramap(_.toString)
  implicit val encodeBigInt: FormEncoder[BigInt] = encodeString.contramap(_.toString)
  implicit val encodeBigDecimal: FormEncoder[BigDecimal] = encodeString.contramap(_.toString)
  implicit val encodeChar: FormEncoder[Char] = encodeString.contramap(_.toString)
  implicit val encodeSymbol: FormEncoder[Symbol] = encodeString.contramap(_.toString)
  implicit val encodeUUID: FormEncoder[UUID] = encodeString.contramap(_.toString)

  implicit def encodeValue[T](implicit ev: ValueOf[T]): FormEncoder[T] =
    (_: T) => Chain(Chain.empty -> ev.value.toString)

  implicit def encodeTraverse[F[_]: Traverse, T](implicit ev: FormEncoder[T]): FormEncoder[F[T]] = (value: F[T]) =>
    Traverse[F].zipWithIndex(value).foldMap { case (v, i) =>
      ev.chained(v).map { case (prefix, v) =>
        prefix.prepend(i.toString) -> v
      }
    }

  implicit inline def encodeProduct[T: Mirror.ProductOf]: FormEncoder[T] = FormEncoder.derived[T]

object EncoderInstances extends EncoderInstances
