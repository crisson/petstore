package me.crisson.petstore.services

import cats.effect.Async
import cats.data.{ EitherT, NonEmptyList, OptionT }
import cats.syntax.option._
import cats.syntax.validated._
import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.monad._
import cats.syntax.traverse._
import cats.syntax.apply._
import cats.instances.list._
import fs2.Stream

import java.util.concurrent.ConcurrentHashMap

import me.crisson.petstore.Inputs
import me.crisson.petstore.models.Pet
import me.crisson.petstore.DomainFailure
import me.crisson.petstore.ResourceInfo
import me.crisson.petstore.ValidatedModels
import me.crisson.petstore.ResourceMeta

trait PetRepo[F[_]] {
  def create(valid: ValidatedModels.Pet): EitherT[F, DomainFailure, Pet]
  def update(id: Pet.Id, valid: ValidatedModels.Pet): EitherT[F, DomainFailure, Pet]
  def updateStatus(id: Pet.Id, valid: ValidatedModels.PetUpdate): EitherT[F, DomainFailure, Pet]
  def addPhoto(id: Pet.Id, meta: ResourceMeta): EitherT[F, DomainFailure, ResourceInfo]
  def delete(id: Pet.Id): EitherT[F, DomainFailure, Unit]
  def get(id: Pet.Id): EitherT[F, DomainFailure, Pet]
}

class PetService[F[_]: Async](storage: StorageService[F], repo: PetRepo[F]) {
  def create(in: Inputs.Pet): EitherT[F, DomainFailure, Pet] = {
    val result = ValidatedModels.Pet.Validator.validate(in)
    for {
      model <- EitherT
        .right[NonEmptyList[String]](result.map(_.toEither))
        .transform(_.joinRight)
        .leftMap[DomainFailure](DomainFailure.ValidationFailed(DomainFailure.Models.Pet, _))
      newPet <- repo.create(model)
      newPhotoMetas = newPet.photoUrls.map(_.meta)
      newPhotos = model.photos
        .collect {
          case (data, meta) if newPhotoMetas.contains(meta) =>
            (Stream.emits[F, Byte](data), newPet.photoUrls(newPhotoMetas.indexOf(meta)))

        }
      _ <- EitherT.pure[F, DomainFailure](newPhotos.map(s => storage.put(s._1, s._2)).toList.sequence)
    } yield newPet
  }

  def update(id: Pet.Id, in: Inputs.Pet): EitherT[F, DomainFailure, Pet] = {
    val result = ValidatedModels.Pet.Validator.validate(in)
    for {
      model <- EitherT
        .right[NonEmptyList[String]](result.map(_.toEither))
        .transform(_.joinRight)
        .leftMap[DomainFailure](DomainFailure.ValidationFailed(DomainFailure.Models.Pet, _))
      newPet <- repo.update(id, model)
      newPhotoMetas = newPet.photoUrls.map(_.meta)
      newPhotos = model.photos
        .collect {
          case (data, meta) if newPhotoMetas.contains(meta) =>
            (Stream.emits[F, Byte](data), newPet.photoUrls(newPhotoMetas.indexOf(meta)))

        }
      _ <- EitherT.pure[F, DomainFailure](newPhotos.map(s => storage.put(s._1, s._2)).toList.sequence)
    } yield newPet
  }

  def updateInfo(id: Pet.Id, raw: Inputs.PetUpdate): EitherT[F, DomainFailure, Pet] =
    for {
      model <- EitherT
        .right[NonEmptyList[String]](ValidatedModels.Pet.StatusUpdateValidator.validate(raw).map(_.toEither))
        .transform(_.joinRight)
        .leftMap[DomainFailure](DomainFailure.ValidationFailed(DomainFailure.Models.Pet, _))
      updatedPet <- repo.updateStatus(id, model)
    } yield updatedPet

  def get(id: Pet.Id): OptionT[F, Pet] = repo.get(id).toOption

  def delete(id: Pet.Id): OptionT[F, Unit] = repo.delete(id).toOption

  def upload(id: Pet.Id, data: Stream[F, Byte], meta: ResourceMeta): EitherT[F, DomainFailure, ResourceInfo.Id] =
    for {
      pet   <- get(id).toRight(DomainFailure.ModelNotFound("Pet", id.toString.some))
      rInfo <- repo.addPhoto(id, meta)
      _     <- EitherT.right[DomainFailure](storage.put(data, rInfo))
    } yield rInfo.id
}
