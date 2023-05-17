
# Formed 3

Helper library to squash generic nested products into a list of fields
for www-form-urlencoded requests, typically to interface with APIs
that don't support JSON.

For Scala 3.

# Quick Usage

Add to your build.sbt:
```scala
libraryDependencies += "io.github.rzqx" % "formed3" % "<version>"
```

Imports:
```scala
import io.github.rzqx.formed.implicits.*
import io.github.rzqx.formed.syntax.*
```

Define your ADT (using Stripe's API as an example) and create an instance:
```scala
final case class LineItem(price: String, quantity: Int)
final case class CheckoutSession(mode: String, line_items: List[LineItem])

val item1 = LineItem("price1", 1)
val item2 = LineItem("price2", 2)
val checkout = CheckoutSession("payment", List(item1, item2))
```

Squash the instance:
```scala
checkout.asFormData
// res0: Chain[Tuple2[String, String]] = Wrap(
//   seq = Vector(
//     ("mode", "payment"),
//     ("line_items[0][price]", "price1"),
//     ("line_items[0][quantity]", "1"),
//     ("line_items[1][price]", "price2"),
//     ("line_items[1][quantity]", "2")
//   )
// )

checkout.asFormUrlEncoded
// res1: String = "mode%3Dpayment%26line_items%5B0%5D%5Bprice%5D%3Dprice1%26line_items%5B0%5D%5Bquantity%5D%3D1%26line_items%5B1%5D%5Bprice%5D%3Dprice2%26line_items%5B1%5D%5Bquantity%5D%3D2"
```

Use in http4s:
```scala
import org.http4s.UrlForm

UrlForm.fromChain(checkout.asFormData)
// res2: UrlForm = HashMap(line_items[0][quantity] -> Chain(1), line_items[1][price] -> Chain(price2), line_items[0][price] -> Chain(price1), mode -> Chain(payment), line_items[1][quantity] -> Chain(2))
```

## Customization

Define an encoder for a new type by converting it into a string inside `contramap`:
```scala
import io.github.rzqx.formed.FormEncoder
import cats.implicits.*
import scala.concurrent.duration.*

implicit val durationEncoder: FormEncoder[Duration] =
  FormEncoder[String].contramap(_.toSeconds.toString)
// durationEncoder: FormEncoder[Duration] = io.github.rzqx.formed.FormEncoder$$anon$1$$Lambda$18251/0x0000000802f5a158@6201c4f
  
final case class Foo(duration: Duration)

Foo(1.hour).asFormDisplay 
// res3: String = "duration=3600"
```

Define a custom prefix encoder to change the way nested fields are encoded:
```scala
import io.github.rzqx.formed.PrefixEncoder

// import only the encoder instances
import io.github.rzqx.formed.instances.EncoderInstances.*

import io.github.rzqx.formed.syntax.*
import cats.data.Chain
import cats.implicits.*

implicit val arrowPrefixEncoder: PrefixEncoder = (value: Chain[String]) =>
  value.deleteFirst(_ => true) match {
    case Some((head, tail)) => head + tail.foldMap(v => s"->$v")
    case None => ""
  }
```

Use the custom prefix encoder:
```scala
final case class LineItem(price: String, quantity: Int)
final case class CheckoutSession(mode: String, line_items: List[LineItem])

CheckoutSession("payment", List(LineItem("price1", 1))).asFormDisplay
// res5: String = """mode=payment
// line_items->0->price=price1
// line_items->0->quantity=1"""
```