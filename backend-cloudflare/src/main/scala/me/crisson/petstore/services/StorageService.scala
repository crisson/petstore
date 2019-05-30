package me.crisson.petstore.services

import fs2.Stream
import cats.effect.Async
import cats.data.{ OptionT }
import cats.syntax.functor._
import java.util.concurrent.ConcurrentHashMap
import me.crisson.petstore.{ ResourceInfo, ResourceMeta }
import java.{ util => ju }
import fs2.Pull
import fs2.concurrent.Queue

trait StorageService[F[_]] {
  def put(resource: Stream[F, Byte], info: ResourceInfo): F[Unit];
  def get(id: ResourceInfo.Id): OptionT[F, List[Byte]]
  def delete(id: ResourceInfo.Id): F[Unit]
}

object StorageService {
  def apply[F[_]: Async]: StorageService[F] = new StorageService[F] {
    private val fs = new ConcurrentHashMap[ResourceInfo.Id, List[Byte]]

    def put(resource: Stream[F, Byte], info: ResourceInfo): F[Unit] =
      resource.compile.toList.map { bytes =>
        val xs = Option(fs.get(info.id)).getOrElse(List()) ++ bytes
        fs.put(info.id, xs)
      }.void

    def get(id: ResourceInfo.Id) = OptionT.pure[F](Option(fs.get(id)).getOrElse(List.empty[Byte]))

    def delete(id: ResourceInfo.Id): F[Unit] =
      Async[F].delay {
        fs.remove(id);
      }.void
  }
}
