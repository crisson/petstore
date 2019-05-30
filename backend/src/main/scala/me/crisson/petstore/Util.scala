package me.crisson.petstore

import cats.data.{ NonEmptyList, Validated }
import cats.Id
import cats.syntax.monad._
import cats.effect.Async

import java.{ util => ju }
import scala.concurrent.ExecutionContext
import cats.effect.ContextShift

sealed trait ContentType
object ContentType {
  object `application/json` extends ContentType
  object `image/png`        extends ContentType
  object `image/jpg`        extends ContentType
  object `image/jpeg`       extends ContentType
}

case class ResourceMeta(size: Long, contentType: ContentType, originalName: String, deduplicatedName: String)

/**
 * Metadata about any storaged resource
 */
case class ResourceInfo(id: ResourceInfo.Id, meta: ResourceMeta)

object ResourceInfo {
  case class Id(value: String) extends AnyVal

  object Id {
    def apply(id: ju.UUID): Id = Id(id.toString)

    def newInstance: Id = Id(ju.UUID.randomUUID().toString())
  }
}

trait ModelValidator[S, U] {
  def validate[F[_]: Async](in: S): F[Validated[NonEmptyList[String], U]]
}

object Queues {
  // queue to place an order
}
