package bot

import bot.PageType.PageType

object WikiPage {

  private val DATE_YEAR_REGEX = "\\d{0,4}".r
  private val DATE_COMPLETE_REGEX = "\\d{0,4}\\.(?:\\d|\\*){0,2}.(?:\\d|\\*){0,2}".r
  private val DATE_INTERVAL_REGEX = "\\d{0,4}(?:-\\d{0,4})?\\.\\d{0,2}(?:-\\d{0,2})?\\.\\d{0,2}(?:-\\d{0,2})?".r
  private val LOCATION_REGEX = "\\d{15,}".r
  private val DATE_UNCONVERTIBLE_REGEX = "-.*".r
  private val WORD_REGEX = "[^#+*/=&%_$£!<>§°\"/`:;]+".r

  def getTypeOfArticle(title: String): PageType = title match {
    case DATE_YEAR_REGEX() | DATE_COMPLETE_REGEX() | DATE_INTERVAL_REGEX() => PageType.DATE
    case LOCATION_REGEX() => PageType.LOCATION
    case DATE_UNCONVERTIBLE_REGEX() => PageType.NONE
    case WORD_REGEX() => PageType.ARTICLE
    case _ => PageType.NONE
  }

}

class WikiPage(
                val title: String,
                val timestamp: Int,
                val revisionId: Int,
                val editor: String,
                val editSummary: String,
                val pageType: PageType,
                val keywords: List[String],
                val images: List[WikiImage],
                val ignored: List[String]) {

  def isMaxImageReached = ignored.length > 5

  override def toString: String = {
    ("\t\t{\n" +
      "\t\t\t\"title\" : \"%s\",\n" +
      "\t\t\t\"timestamp\" : %d,\n" +
      "\t\t\t\"revisionId\" : %d,\n" +
      "\t\t\t\"editor\" : \"%s\",\n" +
      "\t\t\t\"editSummary\" : \"%s\",\n" +
      "\t\t\t\"pageType\" : \"%s\",\n" +
      "\t\t\t\"keywords\" : [\n%s\n" +
      "\t\t\t],\n" +
      "\t\t\t\"images\" : [\n%s\n" +
      "\t\t\t],\n" +
      "\t\t\t\"ignored\" : [\n%s\n" +
      "\t\t\t]\n" +
      "\t\t}\n"
      ).
      format(title, timestamp, revisionId, editor, editSummary, pageType.toString, listToString(keywords), images.mkString(",\n"), listToString(ignored))
  }

  private def listToString[T](raw: List[T]) = raw.map(x => "\t\t\t\t\"" + x + "\"").mkString(", \n")

}


object PageType extends Enumeration {
  type PageType = Value
  val NONE = Value("NONE")
  val ARTICLE = Value("ARTICLE")
  val DATE = Value("DATE")
  val LOCATION = Value("LOCATION")
  val USER = Value("UTILISATEUR")
}