package http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import http.MessagesApi.{AnswerAllRaw, AnswerMinMax, AnswerPrice, DateOilPrice, Error, MinMax, OilPrice, PeriodOilPrice}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait MessagesApi extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val formatDateOilPrice: RootJsonFormat[DateOilPrice] = jsonFormat3(DateOilPrice)
  implicit val formatPeriodOilPrice: RootJsonFormat[PeriodOilPrice] = jsonFormat2(PeriodOilPrice)
  implicit val formatOilPrice: RootJsonFormat[OilPrice] = jsonFormat2(OilPrice)
  implicit val formatMinMax: RootJsonFormat[MinMax] = jsonFormat2(MinMax)
  implicit val formatError: RootJsonFormat[Error] = jsonFormat2(Error)
  implicit val formatAnswerPriceFromDate: RootJsonFormat[AnswerPrice] = jsonFormat1(AnswerPrice)
  implicit val formatAnswerMinMax: RootJsonFormat[AnswerMinMax] = jsonFormat1(AnswerMinMax)
  implicit val formatAnswerAllRaw: RootJsonFormat[AnswerAllRaw] = jsonFormat1(AnswerAllRaw)

}

object MessagesApi {

  case class DateOilPrice(year: Int, month: Int, day: Int)

  case class PeriodOilPrice(dateFrom: Option[DateOilPrice], dateTo: Option[DateOilPrice])

  case class OilPrice(price: Double, periodOilPrice: PeriodOilPrice)

  case class Error(code: Int, message: String)

  case class AnswerPrice(answer: Either[Error, Double])

  case class MinMax(min: Double, max: Double)

  case class AnswerMinMax(answer: Either[Error, MinMax])

  case class AnswerAllRaw(item: Seq[OilPrice])

  ///
  val ERROR_NOT_FOUND_FROM_DATE: Error = Error(1, "Цена за выбранную дату не найдена")
  val ERROR_NOT_ENTER_DATE: Error = Error(2, "не указана(-ы) дата(-ы)")
  val ERROR_UNKNOWN: Error = Error(0, "не известная ошибка")
}
