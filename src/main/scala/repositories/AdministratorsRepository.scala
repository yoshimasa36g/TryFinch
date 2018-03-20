package repositories

import com.twitter.util.Future
import models.Administrator

trait AdministratorsRepository {
  def all: Future[List[Administrator]]
  def findBy(id: String): Future[List[Administrator]]
  def add(administrator: Administrator): Future[Long]
  def update(administrator: Administrator): Future[Long]
  def delete(id: String): Future[Long]
}
