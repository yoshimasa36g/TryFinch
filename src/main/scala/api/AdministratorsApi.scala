package api

import com.twitter.util.Future
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import models.{Administrator, AdministratorApiModel}
import repositories.AdministratorsRepository
import shapeless.{:+:, CNil}
import values.Password

final class AdministratorsApi(repository: AdministratorsRepository) {
  import io.circe._
  implicit val encode: Encoder[Password] = Encoder[String].contramap(_.value)
  implicit val decode: Decoder[Password] = Decoder[String].map(Password)

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
    repository.findBy(a.id) flatMap { targets =>
      targets.length match {
        case 0 => repository.add(a).map(_ => Created())
        case _ => Future { Conflict(new Exception(s"'${a.id}' is existed.")) }
      }
    }
  }

  private val update = post("administrators" :: path("update") :: jsonBody[Administrator]) { a: Administrator =>
    repository.update(a) map { _ =>
      Ok()
    }
  }

  private val delete = post("administrators" :: path[String] :: path("delete")) { id: String =>
    repository.delete(id) map { _ =>
      Ok()
    }
  }

  val routes: Endpoint[:+:[List[AdministratorApiModel], :+:[AdministratorApiModel, :+:[Unit, :+:[Unit, :+:[Unit, CNil]]]]]] = {
    all :+: find :+: add :+: update :+: delete
  }
}
