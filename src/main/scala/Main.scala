import api.AdministratorsApi
import com.twitter.finagle.param.Stats
import com.twitter.finagle.Http
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import database.DbContext
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.circe.generic.auto._
import security.SslCertificate

object Main extends TwitterServer {
  private case class Locale(language: String, country: String)
  private case class Time(locale: Locale, time: String)

  private def currentTime(l: java.util.Locale): String = java.util.Calendar.getInstance(l).getTime.toString

  private val time: Endpoint[Time] = post("time" :: jsonBody[Locale]) { l: Locale =>
    Ok(Time(l, currentTime(new java.util.Locale(l.language, l.country))))
  }

  private val dbContext = DbContext.forDevelopment()

  private val service = new AdministratorsApi(dbContext).routes.toService

  def main(): Unit = {
    val server = Http.server
      .configured(Stats(statsReceiver))
      .withTransport
      .tls(SslCertificate.configuration)
      .serve(":8081", service)

    onExit {
      server.close()
      dbContext.close()
    }

    Await.ready(adminHttpServer)
  }
}
