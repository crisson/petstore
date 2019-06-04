package me.crisson.petstore

import cats.data.{ NonEmptyList, Validated, ValidatedNel }

import cats.instances.list._
import cats.instances.vector._

import cats.syntax.validated._
import cats.syntax.option._
import cats.syntax.either._
import cats.syntax.traverse._
import cats.syntax.apply._
import cats.syntax.applicative._

import cats.effect.Async
import cats.effect.implicits._

import me.crisson.petstore.models.{ Pet => MPet, User => MUser, Email, PhoneNumber }
import me.crisson.petstore.services.PetService
import cats.effect.Resource

object ValidatedModels {
  case class Pet(
      name: String,
      category: MPet.Category,
      tags: Set[MPet.Tag] = Set.empty[MPet.Tag]
  )

  case class PetUpdate(name: String, status: MPet.Status)

  case class User(
      firstName: String,
      lastName: String,
      email: Email,
      password: String,
      phone: PhoneNumber,
      status: MUser.Status
  )

  object Pet {
    type ResourceInput = (Vector[Byte], ResourceMeta)

    object StatusUpdateValidator extends ModelValidator[Inputs.PetUpdate, ValidatedModels.PetUpdate] {
      def validate[F[_]: Async](raw: Inputs.PetUpdate): F[ValidatedNel[String, ValidatedModels.PetUpdate]] =
        Async[F].delay((Validator.petName(raw.name), status(raw.status)).mapN(PetUpdate.apply _))

      def status(in: String): ValidatedNel[String, MPet.Status] =
        MPet.Status(in).toValidatedNel

    }
    object Validator extends ModelValidator[Inputs.Pet, ValidatedModels.Pet] {
      private val MaxImageSize = 10000000

      private val MaxOriginalNameSize = 48

      private val MaxPetNameLength = 96

      private val ContentTypes = Set(ContentType.`image/jpg`, ContentType.`image/png`, ContentType.`image/jpeg`)

      def validate[F[_]: Async](raw: Inputs.Pet): F[ValidatedNel[String, ValidatedModels.Pet]] = {
        val Inputs.Pet(name, rawCat, rawTags) = raw
        Async[F].delay(
          (
            petName(name),
            category(rawCat),
            rawTags.map(tag).toList.traverse(identity).map(_.toSet)
          ).mapN(ValidatedModels.Pet.apply _)
        )
      }

      def photos(pics: List[ResourceInput]): ValidatedNel[String, Seq[ResourceInput]] = {
        val (bytes, metas)               = pics.toVector.unzip
        val (validImages, invalidImages) = metas.map(resource).partition(_.isValid)
        val resources = validImages.sequence.map(
          m =>
            m.map { r =>
              val idx = metas.indexOf(r)
              (bytes(idx), r)
            }
        )

        resources
      }

      def petName(name: String): ValidatedNel[String, String] =
        if (Option(name).isEmpty || name.trim.length > 0) "Pet name is required".invalidNel
        else if (name.trim.length > MaxPetNameLength) s"Pet name must be less than $MaxPetNameLength".invalidNel
        else name.trim.validNel

      def resource(meta: ResourceMeta): ValidatedNel[String, ResourceMeta] =
        if (meta.size > MaxImageSize) s"Image ${meta.originalName.substring(MaxOriginalNameSize)} is invalid".invalidNel
        else if (!ContentTypes.contains(meta.contentType))
          s"This image type is not supported.  The following content-types are supported: ${ContentTypes.mkString(", ")} are supported".invalidNel
        else meta.validNel[String]

      def category(raw: String): ValidatedNel[String, MPet.Category] =
        if (Option(raw).isEmpty) "Pet category is required".invalidNel
        else if (raw.trim.isEmpty) "Pet category is required".invalidNel
        else MPet.Category.get(raw).toValidNel("Pet category does not exist")

      def tag(raw: String): ValidatedNel[String, MPet.Tag] = {
        if (Option(raw).isEmpty) "Invalid tag value".invalidNel
        if (raw.trim.isEmpty) "Tag cannot be empty".invalidNel
        else MPet.Tag(raw.trim).validNel[String]
      }

    }
  }
}
