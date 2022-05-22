package http

import logic.OilLogic.{getAll, getAveragePriceFromPeriod, getMaxMin, getPriseFromDate}
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{RequestEntity, ResponseEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import http.MessagesApi.{DateOilPrice, Error, PeriodOilPrice}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class OilPriceHttpServerRoute(implicit val actorSystem: ActorSystem, implicit val execCtx: ExecutionContext, implicit val log: LoggingAdapter) extends MessagesApi {

  private[http] def route: Route = {
    new OilPriceHttpServerRoute().routes
  }

  def routes: Route = {

      path("getFromDate") {
        get {
          extractRequest { request =>
            val processingResult = Unmarshal(request).to[DateOilPrice]
              .map(date => Marshal.apply(getPriseFromDate(date)).to[RequestEntity])
              .recoverWith { case e: Throwable =>
                Future.successful {
                  Marshal.apply(getErrorFromThrowable(e)).to[RequestEntity]
                }
              }

            onComplete(processingResult)
              .apply(complete(_))
          }
        }
      } ~
      path("getAverageFromPeriod") {
        get {
          extractRequest { request =>
            val processingResult = Unmarshal(request).to[PeriodOilPrice]
              .map(period => Marshal.apply(getAveragePriceFromPeriod(period)).to[RequestEntity])
              .recoverWith { case e: Throwable =>
                Future.successful {
                  Marshal.apply(getErrorFromThrowable(e)).to[RequestEntity]
                }
              }

            onComplete(processingResult)
              .apply(complete(_))
          }
        }
      } ~
      path("getMaxAndMin") {
        get {
          extractRequest { request =>
            val processingResult = Unmarshal(request).to[PeriodOilPrice]
              .map(period => Marshal.apply(getMaxMin(period)).to[RequestEntity])
              .recoverWith { case e: Throwable =>
                Future.successful {
                  Marshal.apply(getErrorFromThrowable(e)).to[RequestEntity]
                }
              }
            onComplete(processingResult)
              .apply(complete(_))
          }
        }
      } ~
      path("getAll") {
        get {
            complete(
              Marshal.apply(getAll).to[RequestEntity]
            )
        }
      }
  }

  private def getErrorFromThrowable(e: Throwable) = {
    Error(0, "Неизвестная ошибка" + e.getMessage)
  }
}
