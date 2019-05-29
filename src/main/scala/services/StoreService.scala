package me.crisson.petstore.services

import cats.effect.Async
import cats.data.{ EitherT, OptionT }

import me.crisson.petstore.Inputs
import me.crisson.petstore.DomainFailure
import me.crisson.petstore.ResourceInfo
import me.crisson.petstore.models.{ Order, Pet, Store }

class StoreService[F[_]: Async](pet: PetService[F]) {
  def inventory(id: Store.Id): EitherT[F, DomainFailure, Store.Inventory] = ???
  def order(pet: Pet.Id): EitherT[F, DomainFailure, Order]                = ???
  def get(id: Order.Id): OptionT[F, Order]                                = ???
  def delete(id: Order.Id): OptionT[F, Unit]                              = ???
}
