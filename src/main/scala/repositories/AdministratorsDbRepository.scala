package repositories

import com.twitter.util.Future
import database.{DbContext, Schema}
import models.Administrator

final class AdministratorsDbRepository(val context: DbContext) extends AdministratorsRepository with Schema {
  import context._

  override def all: Future[List[Administrator]] = context.run(administrators)

  override def findBy(id: String): Future[List[Administrator]] = context.run(
    administrators.filter(_.id == lift(id)).take(1)
  )

  override def add(administrator: Administrator): Future[Long] = context.run(
    administrators.insert(lift(administrator.encrypted()))
  )

  override def update(administrator: Administrator): Future[Long] = context.run(
    administrators.filter(_.id == lift(administrator.id))
      .update(lift(administrator.encrypted()))
  )

  override def delete(id: String): Future[Long] = context.run(
    administrators.filter(_.id == lift(id)).delete
  )
}
