package repositories

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

  "AdministratorsDbRepository" should "can get all administrators" in {
    val administrators = Await.result(repository.all)

    assert(administrators.length >= 3)
  }

  it should "can get specific administrator" in {
    val administrators = Await.result(repository.findBy("112"))
    val expectedLength = 1
    val actualLength = administrators.length
    assert(expectedLength == actualLength)

    val expected = Administrator("112", Password("p2"), "test2", "b@c.d")
    val actual = administrators.head
    assert(expected.equals(actual))
  }

  it should "can add administrator" in {
    val newAdministrator = Administrator("new", Password("pass"), "new-admin", "aaa@example.com")
    val encrypted = newAdministrator.encrypted()
    Await.ready(repository.add(encrypted))
    val added = Await.result(repository.all).find(_.id == "new").get

    assert(encrypted == added)
  }

  it should "can update administrator" in {
    val updateData = Administrator("113", Password("new-password"), "new-name", "new-email").encrypted()
    Await.ready(repository.update(updateData))
    val updated = Await.result(repository.all).find(_.id == "113").get

    assert(updateData == updated)
  }

  it should "can delete administrator" in {
    Await.ready(repository.delete("111"))
    val deleted = Await.result(repository.all).find(_.id == "111")

    assert(deleted.isEmpty)
  }
}
