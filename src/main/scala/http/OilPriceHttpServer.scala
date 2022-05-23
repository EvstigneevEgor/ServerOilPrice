package http

import akka.actor._
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl._
import akka.http.scaladsl.server.RouteResult._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class OilPriceHttpServer {
  implicit val actorSystem: ActorSystem = ActorSystem.apply("MyHttpServer")
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  implicit val timeout: Timeout = Timeout(15.seconds)
  implicit val log: LoggingAdapter = Logging(actorSystem, "main")

  def startHttp(address: String, port: Int): Future[ServerBinding] = {
    Http().newServerAt(address, port).bindFlow(new OilPriceHttpServerRoute().route)
  }

  def stop(http: Future[Http.ServerBinding]): Unit = {
    http
      .flatMap(_.unbind())(actorSystem.dispatcher)
      .onComplete(_ => {
        log.info(s"HTTP listener unbound $splitter")
      })
  }

}
