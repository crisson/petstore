package me.crisson.petstore

import cats.effect._
import cats.implicits._
import org.http4s.server.blaze._
import org.http4s.implicits._
import me.crisson.petstore.services.InMemoryPetRepo
import me.crisson.petstore.services.{ PetRepo, PetService, StorageService }
import me.crisson.petstore.api.controllers.{ Controller, Controllers, PetsController }

object Main extends IOApp {
  val petRepo: PetRepo[IO] = new InMemoryPetRepo[IO]()
  val storage              = StorageService[IO]
  val petSvc               = new PetService[IO](storage, petRepo)
  val ctrl                 = new Controllers[IO](new PetsController[IO](petSvc))

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(ctrl.api.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
