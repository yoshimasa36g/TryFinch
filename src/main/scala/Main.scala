import api.AdministratorsApi
import com.twitter.finagle.Http
import com.twitter.finagle.param.Stats
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import database.DbContext
import io.circe.generic.auto._
import io.finch.circe._
import repositories.AdministratorsDbRepository
import security.SslCertificate

object Main extends TwitterServer {
  private val dbContext = DbContext.forDevelopment()
  private val repository = new AdministratorsDbRepository(dbContext)

  private val service = new AdministratorsApi(repository).routes.toService

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
