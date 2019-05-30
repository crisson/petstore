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

class InMemoryPetRepo[F[_]: Async]() extends PetRepo[F] {
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
