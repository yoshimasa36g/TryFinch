package repositories

import com.twitter.util.Future
import database.{DbContext, Schema}
import models.Administrator

final class AdministratorsRepository(val context: DbContext) extends Schema {
  import context._

  def all: Future[List[Administrator]] = context.run(administrators)

  def findBy(id: String): Future[List[Administrator]] = context.run(
    administrators.filter(_.id == lift(id)).take(1)
  )

  def add(administrator: Administrator): Future[Long] = context.run(
    administrators.insert(lift(administrator.encrypted()))
  )

  def update(administrator: Administrator): Future[Long] = context.run(
    administrators.filter(_.id == lift(administrator.id))
      .update(lift(administrator.encrypted()))
  )

  def delete(id: String): Future[Long] = context.run(
    administrators.filter(_.id == lift(id)).delete
  )
}
