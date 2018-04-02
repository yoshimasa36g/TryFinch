package repositories

import java.util.UUID

import com.twitter.util.Await
import database.DbContext
import models.Administrator
import org.scalatest._
import values.Password

final class AdministratorsDbRepositorySpec extends FlatSpec with DiagrammedAssertions with BeforeAndAfterAll {
  private val context = DbContext.forTest()
  private val repository = new AdministratorsDbRepository(context)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(context.executeAction("delete from administrators where id in ('111', '112', '113', 'new')"))
    Await.ready(context.executeAction("insert into administrators values ('111', 'p1', 'test1', 'a@b.c')"))
    Await.ready(context.executeAction("insert into administrators values ('112', 'p2', 'test2', 'b@c.d')"))
    Await.ready(context.executeAction("insert into administrators values ('113', 'p3', 'test3', 'c@d.e')"))
  }

  override def afterAll(): Unit = {
    Await.ready(context.executeAction("delete from administrators where id in ('111', '112', '113', 'new')"))
    context.close()
    super.afterAll()
  }

  "AdministratorsDbRepository" should "be able to get all administrators" in {
    val administrators = Await.result(repository.all)

    assert(administrators.length >= 3)
  }

  it should "be able to get specific administrator" in {
    val administrators = Await.result(repository.findBy("112"))
    val expectedLength = 1
    val actualLength = administrators.length
    assert(expectedLength == actualLength)

    val expected = Administrator("112", Password("p2"), "test2", "b@c.d")
    val actual = administrators.head
    assert(expected.equals(actual))
  }

  it should "be able to add administrator" in {
    val newAdministrator = Administrator("new", Password("pass"), "new-admin", "aaa@example.com")
    val encrypted = newAdministrator.encrypted()
    Await.ready(repository.add(encrypted))
    val added = Await.result(repository.all).find(_.id == "new").get

    assert(encrypted == added)
  }

  it should "be able to update password" in {
    val password = Password("new password")
    Await.ready(repository.updatePassword("113", password))
    val updated = Await.result(repository.all).find(_.id == "113").get

    assert(password.authenticate(updated.password.value))
  }

  it should "be able to update name" in {
    val name = "new name"
    Await.ready(repository.updateName("113", name))
    val updated = Await.result(repository.all).find(_.id == "113").get

    assert(updated.name == name)
  }

  it should "be able to update email" in {
    val email = "new email"
    Await.ready(repository.updateEmail("113", email))
    val updated = Await.result(repository.all).find(_.id == "113").get

    assert(updated.email == email)
  }

  it should "be able to delete administrator" in {
    Await.ready(repository.delete("111"))
    val deleted = Await.result(repository.all).find(_.id == "111")

    assert(deleted.isEmpty)
  }
}
