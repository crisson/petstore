package me.crisson.petstore.api.controllers

import cats.effect.Async

import org.http4s.HttpRoutes

trait Controller[F[_]] {
  def api: HttpRoutes[F]
}

class Controllers[F[_]: Async](pets: Controller[F]) extends Controller[F] {
  def api = pets.api
}
