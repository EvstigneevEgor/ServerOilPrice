package utils

import http.MessagesApi.{DateOilPrice, PeriodOilPrice}

import java.time.{Duration, LocalDate}

object DateUtils {

  def isDateIncludeInPeriod(firstDate: DateOilPrice, datePeriod: PeriodOilPrice): Boolean = {
    val firstLocalDate = dateOilPriceToLocalDate(firstDate)
    datePeriod match {
      case PeriodOilPrice(Some(dateFrom), Some(dateTo)) =>
        val fromLocalDate = dateOilPriceToLocalDate(dateFrom)
        val toLocalDate = dateOilPriceToLocalDate(dateTo)
        (fromLocalDate.isBefore(firstLocalDate) && toLocalDate.isAfter(firstLocalDate)) ||
          fromLocalDate == firstLocalDate ||
          toLocalDate == firstLocalDate
      case _ =>
        false

    }
  }

  private def dateOilPriceToLocalDate(dateOilPrice: DateOilPrice) = {
    LocalDate.of(dateOilPrice.year, dateOilPrice.month, dateOilPrice.day)
  }

  def getDaysBetweenDate(dateFromOpt: Option[DateOilPrice], dateToOpt: Option[DateOilPrice]): Long = {
    (dateFromOpt, dateToOpt) match {
      case (Some(dateFrom), Some(dateTo)) =>

        val fromLocalDate = dateOilPriceToLocalDate(dateFrom)
        val toLocalDate = dateOilPriceToLocalDate(dateTo)
        Duration.between(fromLocalDate.atStartOfDay(), toLocalDate.atStartOfDay()).toDays + 1
      case (None, None) => 0L

      case _ => 1L
    }
  }

  def getDateOilPriceFromStr(str1: String): Option[DateOilPrice] = {
    val dateStr = str1.split("\\.")
    val day = dateStr(0).toInt
    val month = getMonthIndex(dateStr(1)).getOrElse(0)
    val year = dateStr(2).toInt + 2000
    Option(DateOilPrice(year, month, day))
  }

  private def getMonthIndex(moth: String): Option[Int] = {
    val indexedMonth = IndexedSeq("янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    indexedMonth.indexOf(moth) match {
      case index if index == -1 => None
      case index => Some(index + 1) // Прибавляем единицу чтобы январь был 1-ым, а не 0-ым
    }
  }
}
