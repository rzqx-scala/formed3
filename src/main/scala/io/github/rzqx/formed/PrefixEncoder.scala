package io.github.rzqx.formed

import cats.data.Chain
trait PrefixEncoder {
  def encode(value: Chain[String]): String
}
