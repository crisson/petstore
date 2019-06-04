package me.crisson.petstore

import tapir._
import tapir.model.StatusCode

object API {
  type APIErrorOutput = (StatusCode, DomainFailure)

  val base = endpoint.in("api" / "v1")

}
