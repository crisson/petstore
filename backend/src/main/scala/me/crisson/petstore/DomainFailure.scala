package me.crisson.petstore

import cats.data.NonEmptyList
import io.circe.Encoder
import io.circe.JsonObject
import io.circe.Json
import io.circe.syntax._

sealed trait DomainFailure extends Product with Serializable {
  def message: String
}

object DomainFailure {
  import io.circe.generic.auto._

  implicit def encodeDomainFailure: Encoder[DomainFailure] = Encoder.instance { a =>
    a match {
      case mnf @ ModelNotFound(_, _) =>
        mnf.asJsonObject.add("message", Json.fromString(mnf.message)).asJson
      case _ => JsonObject.singleton("message", Json.fromString("An unknown error has occurred")).asJson
    }
  }
  object Models {
    val Pet = "Pet"
  }
  case class ModelNotFound(model: String, id: Option[String]) extends DomainFailure {
    val message = s"$model(${id.getOrElse("unknown")}) not found"
  }

  case class ValidationFailed(model: String, errors: NonEmptyList[String]) extends DomainFailure {
    def message = s"$model failed to validate with errors ${errors.toList.mkString(", ")}"
  }
}
