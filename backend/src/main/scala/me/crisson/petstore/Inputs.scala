package me.crisson.petstore

object Inputs {
  case class Pet(
      name: String,
      category: String,
      tags: List[String] = List.empty[String]
  )

  case class UploadPetPhoto(
      photos: List[(Vector[Byte], ResourceMeta)] = List.empty
  )

  case class PPet(
      name: String,
      // photos: List[(Vector[Byte], ResourceMeta)],
      category: String,
      tags: List[String]
  )

  case class PetUpdate(name: String, status: String)

  case class Order(petId: Int, quantity: Int, shipDate: String)

  case class User(
      firstName: String,
      lastName: String,
      email: String,
      password: String,
      phone: String,
      status: String
  )
}
