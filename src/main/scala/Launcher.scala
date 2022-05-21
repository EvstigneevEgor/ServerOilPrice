import akka.http.scaladsl.Http
import http.OilPriceHttpServer

import scala.concurrent.Future

object Launcher extends App {
val myServer = new OilPriceHttpServer()
  private val localHost: String = "127.0.0.1"
  private val port: Int = 8080
  private val eventualServerBinding: Future[Http.ServerBinding] = myServer.startHttp(localHost, port)

}
