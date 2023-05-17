package io.github.rzqx.formed

import cats.*
import cats.data.Chain

import scala.compiletime.*
import scala.deriving.*

import java.net.URLEncoder

trait FormEncoder[T]:
  def chained(value: T): Chain[(Chain[String], String)]

  def encode(value: T, prefixEncoder: Chain[String] => String): Chain[(String, String)] =
    chained(value).map { case (prefix, v) =>
      prefixEncoder(prefix) -> v
    }

  def toDisplay(value: T, prefixEncoder: Chain[String] => String): String =
    encode(value, prefixEncoder).map { case (k, v) => s"$k=$v" }.toList
      .mkString("\n")

  def toUrlEncoded(value: T, prefixEncoder: Chain[String] => String): String =
    val combined = encode(value, prefixEncoder).map { case (k, v) => s"$k=$v" }.toList
      .mkString("&")

    URLEncoder.encode(combined, "UTF-8")

object FormEncoder:
  def apply[T](using ev: FormEncoder[T]): FormEncoder[T] = ev

  def encode[T: FormEncoder](value: T)(using pe: PrefixEncoder): Chain[(String, String)] =
    FormEncoder[T].encode(value, pe.encode)

  def toUrlEncoded[T: FormEncoder](value: T)(using pe: PrefixEncoder): String =
    FormEncoder[T].toUrlEncoded(value, pe.encode)

  def toDisplay[T: FormEncoder](value: T)(using pe: PrefixEncoder): String =
    FormEncoder[T].toDisplay(value, pe.encode)

  given Contravariant[FormEncoder] = new Contravariant[FormEncoder]:
    override def contramap[A, B](fa: FormEncoder[A])(f: B => A): FormEncoder[B] = (v: B) => fa.chained(f(v))

  final private inline def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) => constValue[t].asInstanceOf[String] :: summonLabels[ts]

  final private inline def summonEncoders[T <: Tuple]: List[FormEncoder[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) => summonEncoder[t] :: summonEncoders[ts]

  final private inline def summonEncoder[A]: FormEncoder[A] =
    summonFrom {
      case fe: FormEncoder[A] => fe
      case _: Mirror.ProductOf[A] => FormEncoder.derived[A]
    }

  final inline def derived[A](using mirror: Mirror.ProductOf[A]): FormEncoder[A] =
    new FormEncoder[A]:
      lazy val elemLabels: List[String] = summonLabels[mirror.MirroredElemLabels]
      lazy val elemEncoders: List[FormEncoder[?]] = summonEncoders[mirror.MirroredElemTypes]

      def encodeElemAt(index: Int, elem: Any): Chain[(Chain[String], String)] =
        elemEncoders(index).asInstanceOf[FormEncoder[Any]].chained(elem).map { case (prefix, v) =>
          prefix.prepend(elemLabels(index)) -> v
        }

      override def chained(value: A): Chain[(Chain[String], String)] =
        val product = value.asInstanceOf[Product]
        Iterable
          .tabulate(product.productArity) { index =>
            encodeElemAt(index, product.productElement(index))
          }
          .foldLeft(Chain.empty[(Chain[String], String)]) { case (acc, chain) =>
            acc ++ chain
          }
