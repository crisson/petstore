package crisson.tapir
package models

import java.time.LocalDateTime
import io.circe.{Encoder, Decoder}

sealed trait Category {
  def id: Int
  def name: String
}
case class BasicCategory(id: Int, name: String) extends Category

sealed trait PetStatus {}

object PetStatus {
  case object Taken    extends PetStatus
  case object Reserved extends PetStatus
  case object InStore  extends PetStatus

  implicit val encodePetStatus: Encoder[PetStatus] = Encoder.encodeString.contramap[PetStatus](_.toString)
  implicit val decodePetStatus: Decoder[PetStatus] = Decoder.decodeString.emap { str =>
    str match {
      case "Taken"    => Right(Taken)
      case "Reserved" => Right(Reserved)
      case "InStore"  => Right(InStore)
      case _          => Left("Unknown Pet status")
    }
  }
}

case class PetId(value: Long)             extends AnyVal
case class PhotoUrl(value: String)        extends AnyVal
case class PetTag private (value: String) extends AnyVal
object PetTag {
  def apply(value: String): PetTag = PetTag(value.toLowerCase())
}

case class Pet(id: PetId, category: Category, photoUrls: List[PhotoUrl], tags: List[PetTag], status: PetStatus) {}

case class StoreId(value: Int) extends AnyVal

object Store {
  type Inventory = Map[PetStatus, List[Pet]]
}

case class OrderId(value: Int) extends AnyVal
case class Order(id: OrderId, petId: PetId, quantity: Int, shipDate: LocalDateTime, complete: Boolean) {}

case class Email(value: String) extends AnyVal {
  def domain     = value.dropWhile(_ != '@')
  def name       = value.takeWhile(_ != '@')
  def components = value.partition(_ == '@')
}

case class PhoneNumber(value: String) extends AnyVal

sealed trait UserStatus
object UserStatus {
  case object RegisteredNotVerified extends UserStatus
  case object Archived              extends UserStatus
}

case class UserId(value: Int) extends AnyVal
case class User(
    id: UserId,
    firstName: String,
    lastName: String,
    email: Email,
    password: String,
    phone: PhoneNumber,
    status: UserStatus
)
