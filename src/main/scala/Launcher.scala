import http.OilPriceHttpServer

object Launcher extends App {
  val myServer = new OilPriceHttpServer()
  private val localHost: String = "127.0.0.1"
  private val port: Int = 8080
  myServer.startHttp(localHost, port)

}
