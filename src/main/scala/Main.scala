import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import security.SslCertificate

object Main extends App {
  private case class Locale(language: String, country: String)
  private case class Time(locale: Locale, time: String)

  private def currentTime(l: java.util.Locale): String = java.util.Calendar.getInstance(l).getTime.toString

  private val time: Endpoint[Time] = post("time" :: jsonBody[Locale]) { l: Locale =>
    Ok(Time(l, currentTime(new java.util.Locale(l.language, l.country))))
  }

  private val server = Http.server
    .withTransport
    .tls(SslCertificate.configuration)
    .serve(":8081", time.toService)

  Await.ready(server)
}
