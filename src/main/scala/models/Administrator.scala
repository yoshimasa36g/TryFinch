package models

import values.Password

final case class Administrator(id: String, password: Password, name: String, email: String) {
  def encrypted(): Administrator = Administrator(id, password.encrypted(), name, email)

  def authenticate(id: String, passwordHash: String): Boolean =
    (this.id == id) && password.authenticate(passwordHash)
}
