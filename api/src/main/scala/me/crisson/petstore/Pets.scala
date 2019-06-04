package me.crisson.petstore

import models.Pet
import tapir._
import tapir.json.circe._
import io.circe.generic.auto._
import tapir.Schema.SObjectInfo
import tapir.model.StatusCode

object PetsApi extends PetsImplicits {

  object Params {
    val idParameter = path[Int]("id").mapTo((raw: Int) => Pet.Id(raw.toString))
  }

  val base: Endpoint[Unit, (StatusCode, DomainFailure), Unit, Nothing] =
    API.base.in("pets").errorOut(statusCode.and(jsonBody[DomainFailure]))

  val create = base.post
    .in(jsonBody[Inputs.Pet])
    .out(jsonBody[Pet])

  val update =
    base.post
      .in(Params.idParameter)
      .in(jsonBody[Inputs.PPet])
      .out(jsonBody[Pet])

  val updateInfo =
    base.post.in(Params.idParameter).in(jsonBody[Inputs.PetUpdate]).out(jsonBody[Pet])

  val findByStatus =
    base.get.in("findByStatus" / path[String]).out(jsonBody[Pet])

  val get: Endpoint[Pet.Id, API.APIErrorOutput, Pet, Nothing] =
    base.get.in(Params.idParameter).out(jsonBody[Pet])

  val delete = base.delete.in(Params.idParameter).errorOut(jsonBody[DomainFailure]).out(jsonBody[Pet])

  val petListing: Endpoint[Unit, API.APIErrorOutput, List[Pet], Nothing] =
    base.get.in("pets").out(jsonBody[List[Pet]])
}
