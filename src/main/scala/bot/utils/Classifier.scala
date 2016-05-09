package bot.utils

import bot.wiki.PageType
import bot.wiki.PageType.PageType

object Classifier {

  private val dateYear = "\\d{0,4}".r
  private val dateComplete = "\\d{0,4}\\.(?:\\d|\\*){0,2}.(?:\\d|\\*){0,2}".r
  private val dateInterval = "\\d{0,4}(?:-\\d{0,4})?\\.\\d{0,2}(?:-\\d{0,2})?\\.\\d{0,2}(?:-\\d{0,2})?".r
  private val temporalRef = "-?\\d{5,14}".r
  private val locationRef = "\\d{15,}".r
  private val literal = "[^#+*/=&%_$£!<>§°/`:;]+".r

  def findPageType(title: String): PageType = title match {
    case dateYear() | dateComplete() | dateInterval() | temporalRef() => PageType.DATE
    case locationRef() => PageType.LOCATION
    case literal() => PageType.LITERAL
    case _ => PageType.UNCLASSIFIED
  }

}
