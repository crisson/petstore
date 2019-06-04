package me.crisson.petstore
package models

// import io.circe.Json
// import io.circe.syntax._
// import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder }

import java.time.LocalDateTime
import java.{ util => ju }

case class Pet(
    id: Pet.Id,
    name: String,
    category: Pet.Category,
    photoUrls: List[ResourceInfo],
    tags: Set[Pet.Tag],
    status: Pet.Status
) {}

object Pet {

  // implicit val encoder: Encoder[Pet] = Encoder.instance(
  //   p =>
  //     Json.obj(
  //       ("id", Json.fromString(p.id.value)),
  //       ("name", Json.fromString(p.name)),
  //       ("category", p.category.asJson),
  //       ("tags", p.tags.asJson),
  //       ("status", p.status.asJson)
  //     )
  // )

  // implicit val decoder: Decoder[Pet] = Decoder.instance(
  //   c =>
  //     for {
  //       rawId <- c.downField("id").as[String]
  //       id = Pet.Id(rawId)
  //       name     <- c.downField("name").as[String]
  //       category <- c.downField("category").as[Category]
  //       tags     <- c.downField("tags").as[Set[Tag]]
  //       status   <- c.downField("status").as[Status]
  //     } yield {
  //       Pet(id, name, category, photoUrls = List.empty[ResourceInfo], tags, status)
  //     }
  // )

  case class Id(value: String)  extends AnyVal
  case class Tag(value: String) extends AnyVal

  object Id {
    implicit val encodePetId: Encoder[Id] = Encoder.encodeString.contramap(_.value)

    def newInstance: Pet.Id = Pet.Id(ju.UUID.randomUUID().toString())
  }

  object Tag {
    implicit val encodeTag: Encoder[Tag] = Encoder.encodeString.contramap(_.value)

    def apply(value: String): Tag = Tag(value.toLowerCase())
  }
  sealed trait Category {
    def id: String
    def name: String
  }
  case class BasicCategory(id: String, name: String) extends Category
  object Category {
    private val categories                = Map(('dog -> BasicCategory("dog", "dog")));
    def get(id: String): Option[Category] = categories.get(Symbol(id))
  }

  sealed trait Status {}
  case object Taken    extends Status
  case object Reserved extends Status
  case object InStore  extends Status
  case object Unknown  extends Status

  object Status {
    implicit val encodePetStatus: Encoder[Status] = Encoder.encodeString.contramap[Status](_.toString)
    implicit val decodePetStatus: Decoder[Status] = Decoder.decodeString.emap { str =>
      str match {
        case "Taken"    => Right(Taken)
        case "Reserved" => Right(Reserved)
        case "InStore"  => Right(InStore)
        case _          => Left("Unknown Pet status")
      }
    }

    def apply(in: String): Either[String, Status] = in match {
      case "Taken"    => Right(Taken)
      case "Reserved" => Right(Reserved)
      case "InStore"  => Right(InStore)
      case _          => Left("Unknown Pet status")
    }
  }
}

object Store {
  case class Id(value: String) extends AnyVal
  type Inventory = Map[Pet.Status, List[Pet]]
}

case class Order(id: Order.Id, petId: Pet.Id, quantity: Int, shipDate: LocalDateTime, complete: Boolean) {}

object Order {
  case class Id(value: String) extends AnyVal
}

case class Email(value: String) extends AnyVal {
  def domain     = value.dropWhile(_ != '@')
  def name       = value.takeWhile(_ != '@')
  def components = value.partition(_ == '@')
}

case class PhoneNumber(value: String) extends AnyVal

case class User(
    id: User.Id,
    firstName: String,
    lastName: String,
    email: Email,
    password: String,
    phone: PhoneNumber,
    status: User.Status
)

object User {
  case class Id(value: String) extends AnyVal

  sealed trait Status
  object Status {
    case object RegisteredNotVerified extends Status
    case object Archived              extends Status
  }

}
