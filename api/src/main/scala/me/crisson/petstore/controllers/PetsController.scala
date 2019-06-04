package me.crisson.petstore.api.controllers

import cats.effect.{ Async, ContextShift }
import org.http4s.implicits._
import tapir.server.http4s._
import me.crisson.petstore.PetsApi

import me.crisson.petstore.services.PetService
import me.crisson.petstore.DomainFailure

class PetsController[F[_]: Async](petsSvc: PetService[F])(
    implicit val cs: ContextShift[F],
    implicit val so: Http4sServerOptions[F]
) extends Controller[F] {
  def api = get

  def get = PetsApi.get.toRoutes { id =>
    petsSvc
      .get(id)
      .toRight(
        (
          tapir.model.StatusCodes.NotFound,
          DomainFailure.ModelNotFound(DomainFailure.Models.Pet, Option(id.value)): DomainFailure
        )
      )
      .value
  }

}
