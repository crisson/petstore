package crisson.tapir

import fs2.Stream
import cats.effect.Async
import cats.data.{ OptionT }
import cats.syntax.functor._
import java.util.concurrent.ConcurrentHashMap
import java.{ util => ju }
import fs2.Pull
import fs2.concurrent.Queue

trait StorageService[F[_]] {
  def put(resource: Stream[F, Byte], meta: ResourceMeta): F[ResourceInfo.Id];
  def get(id: ResourceInfo.Id): OptionT[F, List[Byte]]
  def delete(id: ResourceInfo.Id): F[Unit]
}

object StorageService {
  def apply[F[_]: Async]: StorageService[F] = new StorageService[F] {
    private val fs = new ConcurrentHashMap[ResourceInfo.Id, List[Byte]]

    def put(resource: Stream[F, Byte], meta: ResourceMeta): F[ResourceInfo.Id] = resource.compile.toList.map { bytes =>
      val id: ResourceInfo.Id = ResourceInfo.Id.newInstance
      val xs                  = Option(fs.get(id)).getOrElse(List()) ++ bytes

      fs.put(id, xs)
      id
    }

    def get(id: ResourceInfo.Id) = OptionT.pure[F](Option(fs.get(id)).getOrElse(List.empty[Byte]))

    def delete(id: ResourceInfo.Id): F[Unit] = Async[F].delay {
      fs.remove(id);
      ()
    }
  }
}
