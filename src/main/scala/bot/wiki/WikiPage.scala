package bot.wiki

import java.util.Calendar

import bot.utils.IO._
import PageType.PageType
import bot.utils.{IO, Tokenizer}
import bot._
import net.sourceforge.jwbf.core.contentRep.Article
import net.sourceforge.jwbf.mediawiki.actions.editing.FileUpload
import net.sourceforge.jwbf.mediawiki.contentRep.SimpleFile
import scala.collection.JavaConverters._

case class WikiPage private (
                     title: String,
                     revisionId: String,
                     editor: String,
                     editSummary: String,
                     pageType: PageType,
                     keywords: List[String],
                     images: List[WikiImage],
                     ignoredImages: List[String],
                     timestamp: Long
                   ) {

  def this(article: Article) {
    this(
      article.getTitle,
      article.getRevisionId,
      article.getEditor,
      article.getEditSummary,
      WikiPage.findPageType(article),
      Tokenizer.hyperword(article),
      List.empty,
      List.empty,
      Calendar.getInstance().getTimeInMillis
    )
  }

  def isMaxImageReached: Boolean = ignoredImages.length > 5

  def withKeywords: WikiPage = {
    //this.copy(keywords = (keywords ++ Tokenizer.hyperwordTokenizer(Bot.bot.getArticle(title))).distinct)
    this
  }

  def withIgnored(url: String): WikiPage = {
    this.copy(ignoredImages = url :: ignoredImages, images = images.filter(i => i.url != url))
  }

  def withImages(): WikiPage = {
    val images = IO.parseRequest(title)

    val image = images.find(img => !ignoredImages.contains(img.link))

    println(title)

    if (image.isDefined) {
      val path = image.get.saveToFile()
      val paths = List("original.jpg", "thumbnail.jpg").map(path + _)

      try {
          paths.foreach(p => bot.getPerformedAction(new FileUpload(new SimpleFile(p), bot)))
        val article = bot.getArticle(title)

        val originalPath = paths.head.substring(paths.head.lastIndexOf("/") + 1)
        val thumbnailPath = paths(1).substring(paths(1).lastIndexOf("/") + 1)
        val snippet = clearString(image.get.snippet)

        val wikiFile =
          s"""[[File:$originalPath|thumb=$thumbnailPath|alt=Alt|$snippet]]\n\n"""

        if (!article.getText.contains(wikiFile)) {
          article.setText(wikiFile + article.getText)
            article.save()

          val wikiImage = new WikiImage(snippet, image.get.link, originalPath, thumbnailPath)
          this.copy(images = wikiImage :: this.images)
        } else {
          this
        }
      } catch {
        case e: Exception => this
      }
    } else {
      println("ERREUR")
      this
    }
  }

  private def clearString(raw: String): String = raw.replaceAll(".jpg", "").replaceAll(".png", "").replaceAll(".gif", "").replaceAll("fichier:", "").replaceAll("Fichier:", "").replaceAll("file:", "").replaceAll("File:", "")

  override def toString: String = {
    s"""
       |{
       |  "title": ${safeString(title)},
       |  "timestamp": $timestamp,
       |  "revisionId": ${safeString(revisionId)},
       |  "editor": ${safeString(editor)},
       |  "editSummary": ${safeString(editSummary)},
       |  "pageType": ${safeString(pageType.toString)},
       |  "keywords" : ${listToString(keywords)},
       |  "images" : ${if (images.isEmpty) "[]" else "[" + images.mkString(",") + "]"},
       |  "ignored" : ${listToString(ignoredImages)}
       |}
    """.stripMargin
  }

  private def listToString(raw: List[String]) =
    if (raw.isEmpty)
      "[]"
    else
      "[\n" + raw.map("\t\t\t" + safeString(_)).mkString(", \n") + "]"

}

object WikiPage {

  private val DATE_YEAR_REGEX = "\\d{0,4}".r
  private val DATE_COMPLETE_REGEX = "\\d{0,4}\\.(?:\\d|\\*){0,2}.(?:\\d|\\*){0,2}".r
  private val DATE_INTERVAL_REGEX = "\\d{0,4}(?:-\\d{0,4})?\\.\\d{0,2}(?:-\\d{0,2})?\\.\\d{0,2}(?:-\\d{0,2})?".r
  private val LOCATION_REGEX = "\\d{15,}".r
  private val DATE_UNCONVERTIBLE_REGEX = "-.*".r
  private val WORD_REGEX = "[^#+*/=&%_$£!<>§°\"/`:;]+".r
  val atLeastOneChar = """[a-zA-Z]""".r

  val blacklist = config.getStringList("blacklist").asScala.toSet

  def findPageType(article: Article): PageType = {
    val title = article.getTitle
    if (blacklist.contains(title)) PageType.BLACKLISTED
    else title match {
      case DATE_YEAR_REGEX() | DATE_COMPLETE_REGEX() | DATE_INTERVAL_REGEX() => PageType.DATE
      case LOCATION_REGEX() => PageType.LOCATION
      case WORD_REGEX() => PageType.LITERAL
      case DATE_UNCONVERTIBLE_REGEX() => PageType.UNCLASSIFIED
      case _ => PageType.UNCLASSIFIED
    }
  }

}
