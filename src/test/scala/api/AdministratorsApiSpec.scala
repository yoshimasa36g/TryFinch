package api

import com.twitter.io.Buf
import com.twitter.util.{Await, Future}
import io.circe.generic.auto._
import io.finch.{Application, Input}
import io.finch.circe._
import models.{Administrator, AdministratorWithoutPassword}
import org.scalatest.{DiagrammedAssertions, FlatSpec}
import repositories.AdministratorsRepository
import shapeless.{Inl, Inr}
import values.Password

final class AdministratorsApiSpec extends FlatSpec with DiagrammedAssertions {
  private val repository = new MockRepository()
  private val api = new AdministratorsApi(repository).routes

  "AdministratorsApi" should "be able to get all administrators" in {
    val request = Input.get("/administrators")
    val response = api(request).awaitValueUnsafe()
    val administrators = response.get.select[List[AdministratorWithoutPassword]].get

    assert(administrators.length == 3)
  }

  it should "be able to get specific administrator's name" in {
    val request = Input.get("/administrators/id2/name")
    val response = api(request).awaitValueUnsafe().get
    val name = response.select[String].get

    assert(name == "name2")
  }

  it should "be able to get specific administrator's email" in {
    val request = Input.get("/administrators/id3/email")
    val response = api(request).awaitValueUnsafe().get
    response match {
      case Inr(Inr(Inl(email))) => assert(email == "email3")
      case _ => throw new Exception("/administrators/id3/email failed.")
    }
  }

  it should "be able to add new administrator" in {
    val newData = Administrator("new", Password("new password"), "new admin", "new email")
    val post = Input.post("/administrators").withBody[Application.Json](newData)
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy(newData.id))
    assert(administrators.length == 1)
    assert(newData.password.authenticate(administrators.head.password.value))
    assert(administrators.head.name == newData.name)
    assert(administrators.head.email == newData.email)
  }

  it should "be able to update password" in {
    val post = Input.post("/administrators/id3/password/update")
      .withBody[Application.Json]("new password")
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy("id3"))
    assert(administrators.length == 1)
    assert(Password("new password").authenticate(administrators.head.password.value))
  }

  it should "be able to update name" in {
    val post = Input.post("/administrators/id3/name/update")
      .withBody[Application.Json]("new name")
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy("id3"))
    assert(administrators.length == 1)
    assert(administrators.head.name == "new name")
  }

  it should "be able to update email" in {
    val post = Input.post("/administrators/id3/email/update")
      .withBody[Application.Json]("new email")
    api(post).awaitValueUnsafe()

    val administrators = Await.result(repository.findBy("id3"))
    assert(administrators.length == 1)
    assert(administrators.head.email == "new email")
  }

  it should "be able to delete administrator" in {
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

  override def updatePassword(id: String, password: Password): Future[Long] = Future {
    val target = data.filter(_.id == id).head
    data = data.filter(_.id != id)
      .union(List(Administrator(id, password, target.name, target.email)))
    1
  }

  override def updateName(id: String, name: String): Future[Long] = Future {
    val target = data.filter(_.id == id).head
    data = data.filter(_.id != id)
      .union(List(Administrator(id, target.password, name, target.email)))
    1
  }

  override def updateEmail(id: String, email: String): Future[Long] = Future {
    val target = data.filter(_.id == id).head
    data = data.filter(_.id != id)
      .union(List(Administrator(id, target.password, target.name, email)))
    1
  }

  override def delete(id: String): Future[Long] = Future {
    data = data.filter(_.id != id)
    1
  }
}
