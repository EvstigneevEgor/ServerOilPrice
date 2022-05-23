package http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import http.MessagesApi.{DateOilPrice, Error, PeriodOilPrice}
import logic.OilLogic.{getAveragePriceFromPeriod, getMaxMin, getOilPriceFromFile, getPriseFromDate}

import scala.concurrent.{ExecutionContext, Future}

class OilPriceHttpServerRoute(implicit val actorSystem: ActorSystem, implicit val execCtx: ExecutionContext, implicit val log: LoggingAdapter) extends MessagesApi {

  private[http] def route: Route = {
    new OilPriceHttpServerRoute().routes
  }

  def routes: Route = {

    path("getFromDate") {
      get {
        extractRequest { request =>
          val processingResult = Unmarshal(request).to[DateOilPrice]
            .map(date => Marshal(getPriseFromDate(date)).to[RequestEntity])
            .recoverWith { case e: Throwable =>
              Future.successful {
                Marshal(getErrorFromThrowable(e)).to[RequestEntity]
              }
            }

          onComplete(processingResult)(complete(_))
        }
      }
    } ~
      path("getAverageFromPeriod") {
        get {
          extractRequest { request =>
            val processingResult = Unmarshal(request).to[PeriodOilPrice]
              .map(period => Marshal(getAveragePriceFromPeriod(period)).to[RequestEntity])
              .recoverWith { case e: Throwable =>
                Future.successful {
                  Marshal(getErrorFromThrowable(e)).to[RequestEntity]
                }
              }

            onComplete(processingResult)(complete(_))
          }
        }
      } ~
      path("getMaxAndMin") {
        get {
          extractRequest { request =>
            val processingResult = Unmarshal(request).to[PeriodOilPrice]
              .map(period => Marshal(getMaxMin(period)).to[RequestEntity])
              .recoverWith { case e: Throwable =>
                Future.successful {
                  Marshal(getErrorFromThrowable(e)).to[RequestEntity]
                }
              }
            onComplete(processingResult)(complete(_))
          }
        }
      } ~
      path("getAll") {
        get {
          complete(
            Marshal(getOilPriceFromFile).to[RequestEntity]
          )
        }
      }
  }

  private def getErrorFromThrowable(e: Throwable) = {
    Error(0, "Неизвестная ошибка" + e.getMessage)
  }
}
