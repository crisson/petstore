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

object PetRepo {
  def apply[F[_]: Async]: PetRepo[F] = new PetRepo[F] {
    private val store = new ConcurrentHashMap[Pet.Id, Pet]
    // private val tags  = new ConcurrentHashMap[Pet.Id, Set[Pet.Tag]]

    def create(valid: ValidatedModels.Pet): EitherT[F, DomainFailure, Pet] = EitherT.pure[F, DomainFailure] {
      val ValidatedModels.Pet(name, photos, category, tags) = valid
      val id                                                = Pet.Id.newInstance
      val (data, metas)                                     = photos.unzip
      val pet = Pet(
        id,
        name,
        category,
        metas.map(ResourceInfo(ResourceInfo.Id.newInstance, _)).to[List],
        tags.toSet,
        Pet.Status.Unknown
      )

      store.put(id, pet)
      pet
    }

    def update(id: Pet.Id, valid: ValidatedModels.Pet): EitherT[F, DomainFailure, Pet] = {
      val ValidatedModels.Pet(name, photos, category, tags) = valid
      val (data, metas)                                     = photos.unzip
      val resourceInfos                                     = metas.map(ResourceInfo(ResourceInfo.Id.newInstance, _)).to[List]
      for {
        p <- EitherT
          .fromOption[F](Option(store.get(id)), DomainFailure.ModelNotFound("Pet", id.toString().some): DomainFailure)
        newPet <- EitherT.pure[F, DomainFailure](
          p.copy(name = p.name, photoUrls = p.photoUrls ++ resourceInfos, category = category, tags = tags)
        )
        _ <- EitherT.pure[F, DomainFailure](store.put(id, newPet))
      } yield newPet
    }

    def updateStatus(id: Pet.Id, valid: ValidatedModels.PetUpdate): EitherT[F, DomainFailure, Pet] =
      OptionT
        .fromOption[F](Option(store.get(id)))
        .map { pet =>
          val newPet = pet.copy(name = valid.name, status = valid.status)
          store.put(id, newPet)
          newPet
        }
        .toRight[DomainFailure](DomainFailure.ModelNotFound(DomainFailure.Models.Pet, Option(id.toString)))

    def addPhoto(id: Pet.Id, meta: ResourceMeta): EitherT[F, DomainFailure, ResourceInfo] = {
      val newPhotos = ResourceInfo(ResourceInfo.Id.newInstance, meta)
      val out = for {
        newPet <- OptionT
          .fromOption[F](Option(store.get(id)))
          .map(p => p.copy(photoUrls = p.photoUrls :+ newPhotos))
        _            = store.put(id, newPet)
        newPhotoInfo = newPhotos
      } yield newPhotoInfo

      out
        .toRight[DomainFailure](DomainFailure.ModelNotFound(DomainFailure.Models.Pet, Option(id.toString)))
    }

    def delete(id: Pet.Id) = EitherT.fromOption[F](
      Option(store.remove(id)).map(_ => ()),
      DomainFailure.ModelNotFound("Pet", id.toString.some)
    )

    def get(id: Pet.Id) = EitherT.pure[F, DomainFailure](store.get(id))
  }
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
