package me.crisson.petstore

object Inputs {
  case class Pet(
      name: String,
      photos: List[(Vector[Byte], ResourceMeta)] = List.empty[(Vector[Byte], ResourceMeta)],
      category: String,
      tags: Set[String] = Set.empty[String]
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
