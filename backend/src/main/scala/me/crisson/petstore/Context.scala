package me.crisson.petstore

import cats.effect.Async

import me.crisson.petstore.services.PetService

class AppCtx[F[_]: Async](services: AppCtx.Services[F]) {}

object AppCtx {
  class Services[F[_]: Async](pet: PetService[F])
}
