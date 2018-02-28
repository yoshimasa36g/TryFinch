package security

import java.io.File

import com.twitter.finagle.ssl.{ApplicationProtocols, CipherSuites, KeyCredentials}
import com.twitter.finagle.ssl.server.SslServerConfiguration
import com.typesafe.config.ConfigFactory

object SslCertificate {
  private val config = ConfigFactory.load()
  private val certificatePath: String = config.getString("security.certificatePath")
  private val keyPath: String = config.getString("security.keyPath")

  val configuration: SslServerConfiguration = SslServerConfiguration(
    KeyCredentials.CertAndKey(new File(certificatePath), new File(keyPath)),
    cipherSuites = CipherSuites.Unspecified,
    applicationProtocols = ApplicationProtocols.Unspecified
  )
}
