package bot

import java.util.Calendar

import bot.PageType.PageType
import net.sourceforge.jwbf.mediawiki.actions.editing.FileUpload
import net.sourceforge.jwbf.mediawiki.contentRep.SimpleFile
import bot.Bot._
import net.sourceforge.jwbf.core.contentRep.Article

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
                val timestamp: Long,
                val revisionId: String,
                val editor: String,
                val editSummary: String,
                val pageType: PageType,
                var keywords: List[String],
                val images: List[WikiImage],
                val ignored: List[String]) {

  def this(article: Article) {
    this(
      article.getTitle,
      Calendar.getInstance().getTimeInMillis,
      article.getRevisionId,
      article.getEditor,
      article.getEditSummary,
      WikiPage.getTypeOfArticle(article.getTitle),
      List(), List(), List()
    )
  }

  def isMaxImageReached = ignored.length > 5

  def updateKeywords() = {
    keywords = (keywords ::: Tokenizer.hyperwordTokenizer(Bot.bot.getArticle(title))).distinct
  }

  def updateImage() = {
    val images = IOUtils.parseRequest(title)

    val image = images.find(img => !ignored.contains(img.link) &&
      !images.map(w => w.link).contains(img.link))

    if (image.isDefined) {
      val path = image.get.saveToFile()
      List("original.jpg", "thumbnail.jpg").map(path + _).map(p => bot.getPerformedAction(new FileUpload(new SimpleFile(p), bot)))
    }
  }

  override def toString: String = {
    ("\t\t{\n" +
      "\t\t\t\"title\" : \"%s\",\n" +
      "\t\t\t\"timestamp\" : %d,\n" +
      "\t\t\t\"revisionId\" : \"%s\",\n" +
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