package me.crisson.petstore

import me.crisson.petstore.models.Pet
import tapir._

trait PetsImplicits extends ApiImplicits {
  val resourceMetaSchema: Schema = Schema.SProduct(
    Schema.SObjectInfo("me.crisson.petstore.ResourceMeta"),
    List(
      ("size", Schema.SNumber),
      ("contentType", Schema.SString),
      ("originalName", Schema.SString),
      ("deduplicatedName", Schema.SString)
    ),
    List("size", "contentType", "originalName", "deduplicatedName")
  )

  val resourceInfoSchema: Schema = Schema.SProduct(
    Schema.SObjectInfo("me.crisson.petstore.ResourceInfo"),
    List(
      ("id", Schema.SInteger),
      ("meta", resourceMetaSchema)
    ),
    List("id", "meta")
  )

  val categorySchema: Schema = Schema.SProduct(
    Schema.SObjectInfo("me.crisson.petstore.models.Category"),
    List(("id", Schema.SString), ("name", Schema.SString)),
    List("id", "name")
  )

  val petSchema: Schema = Schema.SProduct(
    Schema.SObjectInfo("me.crisson.petstore.models.Pet.Tag"),
    List(
      ("id", Schema.SInteger),
      ("name", Schema.SString),
      ("category", categorySchema),
      ("photoUrls", resourceInfoSchema),
      ("tags", Schema.SArray(Schema.SString)),
      ("status", Schema.SString)
    ),
    List("id", "name", "category", "photoUrls", "tags", "status")
  )

  implicit val resourceMetaSchemaFor: SchemaFor[ResourceMeta] = SchemaFor(resourceMetaSchema)
  implicit val resourceInfoSchemaFor: SchemaFor[ResourceInfo] = SchemaFor(resourceInfoSchema)
  implicit val resourceCategory: SchemaFor[Pet.Category]      = SchemaFor(categorySchema)

}
