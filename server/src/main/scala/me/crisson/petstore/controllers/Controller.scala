package me.crisson.petstore.api.controllers

import cats.effect.Async

import org.http4s.HttpRoutes
import me.crisson.petstore.DomainFailure
import tapir.model.StatusCodes
import me.crisson.petstore.DomainFailure.{ ModelNotFound, UnknownError, ValidationFailed }

trait Controller[F[_]] {
  def api: HttpRoutes[F]

  protected def statusCodeOf(fail: DomainFailure): Int = fail match {
    case ModelNotFound(_, _)    => 404
    case ValidationFailed(_, _) => 400
    case UnknownError(_)        => 500
  }
}

class Controllers[F[_]: Async](pets: Controller[F]) extends Controller[F] {
  def api = pets.api
}
