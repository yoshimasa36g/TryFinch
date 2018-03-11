package database

import models.Administrator

trait Schema {
  val context: DbContext
  import context._

  val administrators: Quoted[EntityQuery[Administrator]] = quote {
    querySchema[Administrator]("administrators")
  }
}
