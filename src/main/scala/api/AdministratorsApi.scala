package api

import com.twitter.util.Await
import database.DbContext
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import models.{Administrator, AdministratorApiModel}
import repositories.AdministratorsRepository
import shapeless.{:+:, CNil}
import values.Password

final class AdministratorsApi(context: DbContext) {
  import io.circe._
  implicit val encode: Encoder[Password] = Encoder[String].contramap(_.value)
  implicit val decode: Decoder[Password] = Decoder[String].map(Password)

  private val repository = new AdministratorsRepository(context)

  private val all = get("administrators") {
    repository.all map { administrators =>
      Ok(administrators.map(_.toApiModel))
    }
  }

  private val find = get("administrators" :: path[String]) { id: String =>
    repository.findBy(id) map { a =>
      a.length match {
        case 0 => NotFound(new Exception(s"'$id' is not found."))
        case _ => Ok(a.head.toApiModel)
      }
    }
  }

  private val add = post("administrators" :: jsonBody[Administrator]) { a: Administrator =>
    Await.result(repository.findBy(a.id)).length match {
      case 0 => Await.result(repository.add(a).map(_ => Created()))
      case _ => Conflict(new Exception(s"'${a.id}' is existed."))
    }
  }

  private val update = post("administrators" :: path("update") :: jsonBody[Administrator]) { a: Administrator =>
    repository.update(a) map { _ =>
      Ok()
    }
  }

  private val delete = post("administrators" :: path("delete") :: path[String]) { id: String =>
    repository.delete(id) map { _ =>
      Ok()
    }
  }

  val routes: Endpoint[:+:[List[AdministratorApiModel], :+:[AdministratorApiModel, :+:[Unit, :+:[Unit, :+:[Unit, CNil]]]]]] = {
    all :+: find :+: add :+: update :+: delete
  }
}
