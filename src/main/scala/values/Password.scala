package values

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

final case class Password(value: String) extends AnyVal {
  private[this] def bcrypt(): BCryptPasswordEncoder = new BCryptPasswordEncoder()

  def encrypted(): Password = {
    val hashed = if (value.length == 60) value else bcrypt().encode(value)
    Password(hashed)
  }

  def authenticate(passwordHash: String): Boolean = bcrypt().matches(value, passwordHash)
}

object Password {
  import io.circe._
  implicit val encode: Encoder[Password] = Encoder[String].contramap(_.value)
  implicit val decode: Decoder[Password] = Decoder[String].map(Password(_))
}
