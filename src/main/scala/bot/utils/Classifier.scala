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

  private val months = List("janvier","février","mars" ,"avril" ,"mai" ,"juin" ,"juillet" ,"août" ,"septembre" ,"octobre" ,"novembre" ,"décembre")

  def containsDateLiteral(title : String) = {
    months.exists(m => title.toLowerCase().split(" ").contains(m))
  }

  def findPageType(title: String): PageType = {
    println(" ############## " + title.toLowerCase())
    val pageClass = title match {
      case dateYear() | dateComplete() | dateInterval() | temporalRef() => PageType.DATE
      case locationRef() => PageType.UNCLASSIFIED
      case literal() =>
        if (containsDateLiteral(title)) PageType.DATE
        else PageType.LITERAL
      case _ =>
        if (months.contains(title.toLowerCase)) PageType.DATE
        else PageType.UNCLASSIFIED
    }
    println(" Class = " + pageClass)
    pageClass
  }

}
