package values

import org.scalatest.{DiagrammedAssertions, FlatSpec}

final class PasswordSpec extends FlatSpec with DiagrammedAssertions {
  "Password" should "can encrypt value" in {
    val password = Password("password")
    val encrypted = password.encrypted()
    assert(encrypted != password)
    assert(encrypted.value.length == 60)
  }

  it should "can authenticate password" in {
    val password = Password("password")
    assert(!password.authenticate("password"))
    assert(password.authenticate("$2a$10$ClaRTt6tq4bYZnUaAvT4Ku/Bbt24RbTWwMJWwAYWuOWp/h.fp2NB2"))
  }
}
