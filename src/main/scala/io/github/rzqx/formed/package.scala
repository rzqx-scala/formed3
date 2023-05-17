package io.github.rzqx

import scala.deriving.*
import scala.compiletime.*

package object formed {
  final private inline def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => constValue[t].asInstanceOf[String] :: summonLabels[ts]
    }

  final private inline def summonEncoders[T <: Tuple]: List[FormEncoder[_]] =
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonEncoder[t] :: summonEncoders[ts]
    }

  final private inline def summonEncoder[A]: FormEncoder[A] =
    summonFrom {
      case fe: FormEncoder[A] => fe
      case _: Mirror.Of[A]    => FormEncoder.derived[A]
    }
}
