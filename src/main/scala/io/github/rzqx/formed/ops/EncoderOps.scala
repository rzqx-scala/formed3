package io.github.rzqx.formed.ops

import io.github.rzqx.formed.{FormEncoder, PrefixEncoder}

import cats.data.Chain

trait EncoderOps {
  implicit final def toEncoderSyntax[T](value: T): EncoderOps.EncoderSyntax[T] = new EncoderOps.EncoderSyntax(value)
}

object EncoderOps {
  implicit class EncoderSyntax[T](val value: T) extends AnyVal {
    def asFormData(implicit ev: FormEncoder[T], pe: PrefixEncoder): Chain[(String, String)] =
      ev.encode(value, pe.encode)
    def asFormUrlEncoded(implicit ev: FormEncoder[T], pe: PrefixEncoder): String = ev.toUrlEncoded(value, pe.encode)
    def asFormDisplay(implicit ev: FormEncoder[T], pe: PrefixEncoder): String = ev.toDisplay(value, pe.encode)
  }
}
