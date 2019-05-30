package me.crisson.petstore.services

import cats.data.{ EitherT, OptionT }
import cats.effect.Async

import me.crisson.petstore.Inputs

import me.crisson.petstore.models.User
import me.crisson.petstore.ValidatedModels
import me.crisson.petstore.DomainFailure

trait UserRepo[F[_]] {
  def create(valid: ValidatedModels.User): EitherT[F, DomainFailure, User]
  def update(id: User.Id, valid: ValidatedModels.User): EitherT[F, DomainFailure, User]
  def delete(id: User.Id): OptionT[F, Unit]
  def get(id: User.Id): OptionT[F, User]
}

class UserService[F[_]: Async]() {
  def create(raw: List[Inputs.User]): EitherT[F, DomainFailure, List[User]] = ???
  def create(raw: Inputs.User): EitherT[F, DomainFailure, User]             = ???
  def update(id: User.Id): EitherT[F, DomainFailure, User]                  = ???
  def get(id: User.Id): OptionT[F, User]                                    = ???
  def delete(id: User.Id): OptionT[F, Unit]                                 = ???
}
