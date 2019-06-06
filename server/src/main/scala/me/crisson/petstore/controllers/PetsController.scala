package me.crisson.petstore.api.controllers

import cats.syntax.semigroupk._
import cats.data.{ EitherT }
import cats.effect.{ Async, ContextShift }
import org.http4s.implicits._
import tapir.server.http4s._
import tapir.model.StatusCodes

import me.crisson.petstore.PetsApi
import me.crisson.petstore.services.PetService
import me.crisson.petstore.DomainFailure
import me.crisson.petstore.models.Pet
import me.crisson.petstore.Inputs

class PetsController[F[_]: Async](petsSvc: PetService[F])(
    implicit val cs: ContextShift[F],
    implicit val so: Http4sServerOptions[F]
) extends Controller[F] {
  def api = petListing <+> create <+> update <+> updateInfo <+> get <+> delete

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

  def delete = PetsApi.delete.toRoutes { id =>
    petsSvc
      .delete(id)
      .leftMap(fail => (statusCodeOf(fail), fail))
      .value
  }

  def petListing =
    PetsApi.petListing.toRoutes(
      _ =>
        EitherT
          .right[List[Pet]](petsSvc.list)
          .leftMap(
            _ =>
              (
                StatusCodes.InternalServerError,
                DomainFailure.UnknownError("An unknown error occurred while attempting to list pets"): DomainFailure
              )
          )
          .value
    )

  def create = PetsApi.create.toRoutes { in =>
    petsSvc.create(in).leftMap(fail => (statusCodeOf(fail), fail)).value
  }

  def update = PetsApi.update.toRoutes {
    case (id, petUpdate) =>
      petsSvc.update(id, petUpdate).leftMap(fail => (statusCodeOf(fail), fail)).value
  }

  def updateInfo = PetsApi.updateInfo.toRoutes {
    case (id, statusUpdate) => petsSvc.updateInfo(id, statusUpdate).leftMap(fail => (statusCodeOf(fail), fail)).value
  }

}
