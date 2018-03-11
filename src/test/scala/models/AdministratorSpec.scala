package models

import org.scalatest.{DiagrammedAssertions, FlatSpec}
import values.Password

final class AdministratorSpec extends FlatSpec with DiagrammedAssertions {
  "Administrator" should "can encrypt password" in {
    val administrator = Administrator("id", Password("password"), "name", "email")
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
}
