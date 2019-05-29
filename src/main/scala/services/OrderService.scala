package me.crisson.petstore.services

import cats.effect.Async
import cats.data.Kleisli

import me.crisson.petstore.AppCtx
import me.crisson.petstore.DomainFailure
import me.crisson.petstore.models.Order

object OrderService {
  def get[F[_]: Async]: Kleisli[F, AppCtx[F], Either[DomainFailure, Order]] = ???
}
