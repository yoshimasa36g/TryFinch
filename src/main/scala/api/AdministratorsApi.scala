package api

import com.twitter.util.Future
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import models.Administrator
import repositories.AdministratorsRepository
import values.Password

final class AdministratorsApi(repository: AdministratorsRepository) {

  private val all = get("administrators") {
    repository.all map { administrators =>
      Ok(administrators.map(_.dropPassword))
    }
  }

  private val getName = get("administrators" :: path[String] :: path("name")) {
    id: String =>
      findBy(id).map({
        case Some(administrator) => Ok(administrator.name)
        case _ => NotFound(new NoSuchElementException(id))
      })
  }

  private val getEmail = get("administrators" :: path[String] :: path("email")) {
    id: String =>
      findBy(id).map({
        case Some(administrator) => Ok(administrator.email)
        case _ => NotFound(new NoSuchElementException(id))
      })
  }

  private def findBy(id: String): Future[Option[Administrator]] = {
    repository.findBy(id) map { a =>
      a.length match {
        case 0 => None
        case _ => Option(a.head)
      }
    }
  }

  private val add = post("administrators" :: jsonBody[Administrator]) { a: Administrator =>
    repository.findBy(a.id) flatMap { targets =>
      targets.length match {
        case 0 => repository.add(a).map(_ => Created())
        case _ => Future { Conflict(new Exception(s"'${a.id}' is existed.")) }
      }
    }
  }

  private val updatePassword =
    post("administrators" :: path[String] :: path("password") :: path("update") :: jsonBody[Password]) {
    (id: String, password: Password) =>
      repository.updatePassword(id, password.encrypted()).map(_ => Ok())
    }

  private val updateName =
    post("administrators" :: path[String] :: path("name") :: path("update") :: jsonBody[String]) {
      (id: String, name: String) =>
        repository.updateName(id, name).map(_ => Ok())
    }

  private val updateEmail =
    post("administrators" :: path[String] :: path("email") :: path("update") :: jsonBody[String]) {
      (id: String, email: String) =>
        repository.updateEmail(id, email).map(_ => Ok())
    }

  private val delete = post("administrators" :: path[String] :: path("delete")) { id: String =>
    repository.delete(id).map(_ => Ok())
  }

  // noinspection TypeAnnotation
  val routes = all :+: getName :+: getEmail :+:
    add :+: updatePassword :+: updateName :+: updateEmail :+: delete
}
