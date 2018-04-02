package repositories

import com.twitter.util.Future
import models.Administrator
import values.Password

trait AdministratorsRepository {
  def all: Future[List[Administrator]]
  def findBy(id: String): Future[List[Administrator]]
  def add(administrator: Administrator): Future[Long]
  def updatePassword(id: String, password: Password): Future[Long]
  def updateName(id: String, name: String): Future[Long]
  def updateEmail(id: String, email: String): Future[Long]
  def delete(id: String): Future[Long]
}
