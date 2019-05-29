package me.crisson.petstore

import cats.data.NonEmptyList

sealed trait DomainFailure extends Product with Serializable {
  def message: String
}

object DomainFailure {
  object Models {
    val Pet = "Pet"
  }
  case class ModelNotFound(model: String, id: Option[String]) extends DomainFailure {
    def message = s"$model(${id.getOrElse("unknown")}) not found"
  }

  case class ValidationFailed(model: String, errors: NonEmptyList[String]) extends DomainFailure {
    def message = s"$model failed to validate with errors ${errors.toList.mkString(", ")}"
  }
}
