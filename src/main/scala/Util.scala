package crisson.tapir

import java.{ util => ju }

object Inputs {
  case class Pet(photos: List[List[Byte]] = List.empty[List[Byte]], tags: Set[String] = Set.empty[String])
}

object ValidatedModules {}

sealed trait ContentType
object ContentType {
  object `application/json` extends ContentType
  object `image/png`        extends ContentType
  object `image/jpg`        extends ContentType
}

case class ResourceMeta(size: Long, contentType: ContentType)

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
