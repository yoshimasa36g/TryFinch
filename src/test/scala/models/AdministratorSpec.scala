package models

import java.util.UUID

import org.scalatest.{DiagrammedAssertions, FlatSpec}
import values.Password

final class AdministratorSpec extends FlatSpec with DiagrammedAssertions {
  def randomAdministrator: Administrator = {
    Administrator(
      UUID.randomUUID.toString,
      Password(UUID.randomUUID.toString),
      UUID.randomUUID.toString,
      UUID.randomUUID.toString)
  }

  "Administrator" should "can encrypt password" in {
    val administrator = randomAdministrator
    val encrypted = administrator.encrypted()

    assert(encrypted.id == administrator.id)
    assert(encrypted.password != administrator.password)
    assert(encrypted.name == administrator.name)
    assert(encrypted.email == administrator.email)
  }

  it should "can authenticate with id and password hash" in {
    val administrator = Administrator("id", Password("password"), "name", "email")
    assert(administrator.authenticate("id", "$2a$10$ClaRTt6tq4bYZnUaAvT4Ku/Bbt24RbTWwMJWwAYWuOWp/h.fp2NB2"))
  }

  it should "can create api model" in {
    val administrator = randomAdministrator
    val apiModel = administrator.toApiModel

    assert(apiModel.id == administrator.id)
    assert(apiModel.name == administrator.name)
    assert(apiModel.email == administrator.email)
  }
}
