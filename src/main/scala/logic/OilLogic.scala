package logic

import http.MessagesApi._
import utils.DateUtils.{getDateOilPriceFromStr, getDaysBetweenDate, isDateIncludeInPeriod}

import scala.io.Source

object OilLogic {
  val dataSource = "src/main/resource/data.csv"

  def getPriseFromDate(dateOilPrice: DateOilPrice): AnswerPrice = {
    val answer =
      getOilPriceFromFile.find { oilPrice =>
        isDateIncludeInPeriod(dateOilPrice, oilPrice.periodOilPrice)
      } match {
        case Some(dateOilPrice) => Right(dateOilPrice.price)
        case None => Left(ERROR_NOT_FOUND_FROM_DATE)
      }
    AnswerPrice(answer)
  }

  def getMaxMin(periodOilPrice: PeriodOilPrice): AnswerMinMax = {
    val data = getOilPriceFromFile

    val averageDateOrError =
      periodOilPrice match {
        case PeriodOilPrice(Some(dateFrom), Some(dateTo)) =>
          val priceFromPeriod = getRawFromPeriod(data, dateFrom, dateTo).map(_.price)
          if (priceFromPeriod.isEmpty) {
            Left(ERROR_NOT_FOUND_FROM_DATE)
          } else {
            Right(MinMax(priceFromPeriod.min, priceFromPeriod.max))
          }
        case _ =>
          Left(ERROR_NOT_ENTER_DATE)
      }
    AnswerMinMax(averageDateOrError)
  }

  def getAveragePriceFromPeriod(periodOilPrice: PeriodOilPrice): AnswerPrice = {
    val data = getOilPriceFromFile

    val averageDateOrError =
      periodOilPrice match {
        case PeriodOilPrice(Some(dateFrom), Some(dateTo)) =>
          // из всех месяцев оставляем только те которые пересекают период
          val sliceRaw = getRawFromPeriod(data, dateFrom, dateTo)
          if (sliceRaw.isEmpty) {
            Left(ERROR_NOT_FOUND_FROM_DATE)
          } else {

            val priceWithCroppedDate = replaceHeadAndLastDate(dateFrom, dateTo, sliceRaw)
            // меняем периоды на кол-во дней
            val priceWithDayCount = priceWithCroppedDate
              .map(raw => (raw.price, getDaysBetweenDate(raw.periodOilPrice.dateFrom, raw.periodOilPrice.dateTo)))

            // всего дней за весь период
            val countDays = priceWithDayCount.map { case (_, daysCount) => daysCount }.sum

            val averagePrice = priceWithDayCount.map {
              case (priceToDate, daysCount) =>
                priceToDate * daysCount / countDays
            }.sum


            Right(averagePrice)
          }
        case _ =>
          Left(ERROR_NOT_ENTER_DATE)
      }
    AnswerPrice(averageDateOrError)
  }

  def getOilPriceFromFile: Seq[OilPrice] = {
    val file = Source.fromFile(dataSource, "UTF-8")
    file.getLines()
      .drop(1)
      .toSeq
      .map(getOilPriceFromRaw)

  }

  private def getRawFromPeriod(data: Seq[OilPrice], dateFrom: DateOilPrice, dateTo: DateOilPrice): Seq[OilPrice] = {
    data
      .slice(
        getIntersectionIndex(data, dateFrom).getOrElse(0),
        getIntersectionIndex(data, dateTo).getOrElse(data.size) + 1
      )
  }

  /**
   * Заменяем в перво элементе выборки дату начала, А в последнем дату конца. для измерения средней стоимости
   */
  private def replaceHeadAndLastDate(dateFrom: DateOilPrice, dateTo: DateOilPrice, sliceData: Seq[OilPrice]): Seq[OilPrice] = {
    val newData =
      sliceData.headOption match {
        case Some(headRaw) =>
          headRaw.copy(
            periodOilPrice = headRaw.periodOilPrice.copy(
              dateFrom = Option(dateFrom)
            )
          ) +: sliceData.drop(1)
        case _ => sliceData
      }

    newData.lastOption match {
      case Some(lastRaw) =>
        newData.dropRight(1) :+
          lastRaw.copy(
            periodOilPrice = lastRaw.periodOilPrice.copy(
              dateTo = Option(dateTo)
            )
          )
      case _ => newData
    }
  }

  /**
   * Ищет индекс период элемента которого пересекает дату
   */
  private def getIntersectionIndex(data: Seq[OilPrice], findDate: DateOilPrice): Option[Int] = {
    data.indexWhere(row => isDateIncludeInPeriod(findDate, row.periodOilPrice)) match {
      case index if index == -1 => None
      case index =>
        Some(index)
    }
  }

  private def getOilPriceFromRaw(str: String): OilPrice = {
    val cells = str.split(";")
    val dateFrom = getDateOilPriceFromStr(cells(0))
    val dateTo = getDateOilPriceFromStr(cells(1))
    val amount = cells(2).replace(",", ".").toDouble
    OilPrice(amount, PeriodOilPrice(dateFrom, dateTo))
  }


}
