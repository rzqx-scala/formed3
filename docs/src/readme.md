
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
```scala mdoc
import io.github.rzqx.formed.implicits.*
import io.github.rzqx.formed.syntax.*
```

Define your ADT (using Stripe's API as an example) and create an instance:
```scala mdoc:silent
final case class LineItem(price: String, quantity: Int)
final case class CheckoutSession(mode: String, line_items: List[LineItem])

val item1 = LineItem("price1", 1)
val item2 = LineItem("price2", 2)
val checkout = CheckoutSession("payment", List(item1, item2))
```

Squash the instance:
```scala mdoc
checkout.asFormData

checkout.asFormUrlEncoded
```

Use in http4s:
```scala mdoc
import org.http4s.UrlForm

UrlForm.fromChain(checkout.asFormData)
```

## Customization

Define an encoder for a new type by converting it into a string inside `contramap`:
```scala mdoc
import io.github.rzqx.formed.FormEncoder
import cats.implicits.*
import scala.concurrent.duration.*

implicit val durationEncoder: FormEncoder[Duration] =
  FormEncoder[String].contramap(_.toSeconds.toString)
  
final case class Foo(duration: Duration)

Foo(1.hour).asFormDisplay 
```

Define a custom prefix encoder to change the way nested fields are encoded:
```scala mdoc:reset:silent
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
```scala mdoc
final case class LineItem(price: String, quantity: Int)
final case class CheckoutSession(mode: String, line_items: List[LineItem])

CheckoutSession("payment", List(LineItem("price1", 1))).asFormDisplay
```