package api

import com.twitter.io.Buf
import com.twitter.util.{Await, Future}
import io.finch.{Application, Input}
import models.{Administrator, AdministratorApiModel}
import org.scalatest.{DiagrammedAssertions, FlatSpec}
import repositories.AdministratorsRepository
import values.Password

final class AdministratorsApiSpec extends FlatSpec with DiagrammedAssertions {
  private val repository = new MockRepository()
  private val api = new AdministratorsApi(repository).routes

  "AdministratorsApi" should "can get all administrators" in {
    val response = api(Input.get("/administrators")).awaitValueUnsafe()
    val administrators = response.get.select[List[AdministratorApiModel]].get

    assert(administrators.length == 3)
  }

  it should "can get specific administrator" in {
    val response = api(Input.get("/administrators/id2")).awaitValueUnsafe()
    val administrator = response.get.select[AdministratorApiModel].get

    assert(administrator.id == "id2")
    assert(administrator.name == "name2")
    assert(administrator.email == "email2")
  }

  it should "can add new administrator" in {
    val post = Input.post("/administrators").withBody[Application.Json](Buf.Utf8(
      "{\"id\":\"new\",\"password\":\"password\",\"name\":\"new admin\",\"email\":\"newadmin@example.com\"}"
    ))
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy("new"))
    assert(administrators.length == 1)
    assert(Password("password").authenticate(administrators.head.password.value))
    assert(administrators.head.name == "new admin")
    assert(administrators.head.email == "newadmin@example.com")
  }

  it should "can update administrator" in {
    val post = Input.post("/administrators/update").withBody[Application.Json](Buf.Utf8(
      "{\"id\":\"id3\",\"password\":\"updated password\","
      + "\"name\":\"updated name\",\"email\":\"updated email\"}"
    ))
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy("id3"))
    assert(administrators.length == 1)
    assert(Password("updated password").authenticate(administrators.head.password.value))
    assert(administrators.head.name == "updated name")
    assert(administrators.head.email == "updated email")
  }

  it should "can delete administrator" in {
    val post = Input.post("/administrators/id1/delete")
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy("id1"))
    assert(administrators.isEmpty)
  }
}

private final class MockRepository extends AdministratorsRepository {
  private var data = List(
    Administrator("id1", Password("password1").encrypted(), "name1", "email1"),
    Administrator("id2", Password("password2").encrypted(), "name2", "email2"),
    Administrator("id3", Password("password3").encrypted(), "name3", "email3"))

  override def all: Future[List[Administrator]] = Future { data }

  override def findBy(id: String): Future[List[Administrator]] = Future {
    data.filter(_.id == id)
  }

  override def add(administrator: Administrator): Future[Long] = Future {
    data = data.union(List(administrator.encrypted()))
    1
  }

  override def update(administrator: Administrator): Future[Long] = Future {
    data = data.filter(_.id != administrator.id).union(List(administrator.encrypted()))
    1
  }

  override def delete(id: String): Future[Long] = Future {
    data = data.filter(_.id != id)
    1
  }
}
