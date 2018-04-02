package repositories

import com.twitter.util.Future
import database.{DbContext, Schema}
import models.Administrator
import values.Password

final class AdministratorsDbRepository(val context: DbContext) extends AdministratorsRepository with Schema {
  import context._

  override def all: Future[List[Administrator]] = context.run(administrators)

  override def findBy(id: String): Future[List[Administrator]] = context.run(
    administrators.filter(_.id == lift(id)).take(1)
  )

  override def add(administrator: Administrator): Future[Long] = context.run(
    administrators.insert(lift(administrator.encrypted()))
  )

  override def updatePassword(id: String, password: Password): Future[Long] = context.run(
    administrators.filter(_.id == lift(id))
      .update(_.password -> lift(password.encrypted()))
  )

  override def updateName(id: String, name: String): Future[Long] = context.run(
    administrators.filter(_.id == lift(id)).update(_.name -> lift(name))
  )

  override def updateEmail(id: String, email: String): Future[Long] = context.run(
    administrators.filter(_.id == lift(id)).update(_.email -> lift(email))
  )

  override def delete(id: String): Future[Long] = context.run(
    administrators.filter(_.id == lift(id)).delete
  )
}
