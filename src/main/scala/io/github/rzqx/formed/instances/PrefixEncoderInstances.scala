package io.github.rzqx.formed.instances

import io.github.rzqx.formed.PrefixEncoder

import cats.data.Chain
import cats.implicits._

trait PrefixEncoderInstances {
  implicit val prefixEncoderDefaultInstance: PrefixEncoder = (value: Chain[String]) =>
    value.deleteFirst(_ => true) match {
      case Some((head, tail)) => head + tail.foldMap(v => s"[$v]")
      case None               => ""
    }
}
