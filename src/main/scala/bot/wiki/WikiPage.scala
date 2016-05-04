package bot.wiki

import java.util.Calendar

import bot._
import bot.providers.GoogleImages
import GoogleImages._
import bot.providers.GoogleImages
import bot.utils.Tokenizer
import bot.wiki.PageType.PageType
import net.sourceforge.jwbf.core.contentRep.Article

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
      WikiPage.findPageType(article.getTitle),
      Tokenizer.hyperword(article),
      List.empty,
      List.empty,
      Calendar.getInstance().getTimeInMillis
    )
  }

  def isMaxImageReached: Boolean = ignoredImages.length > 5

  /*def withKeywords: WikiPage = {
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
  }*/

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

  private val dateYear = "\\d{0,4}".r
  private val dateComplete = "\\d{0,4}\\.(?:\\d|\\*){0,2}.(?:\\d|\\*){0,2}".r
  private val dateInterval = "\\d{0,4}(?:-\\d{0,4})?\\.\\d{0,2}(?:-\\d{0,2})?\\.\\d{0,2}(?:-\\d{0,2})?".r
  private val temporalRef = "-?\\d{5,14}".r
  private val locationRef = "\\d{15,}".r
  private val literal = "[^#+*/=&%_$£!<>§°/`:;]+".r

  val blacklist = config.getStringList("blacklist").asScala.toSet

  def findPageType(title: String): PageType = {
    if (blacklist.contains(title)) PageType.BLACKLISTED
    else title match {
      case dateYear() | dateComplete() | dateInterval() | temporalRef() => PageType.DATE
      case locationRef() => PageType.LOCATION
      case literal() => PageType.LITERAL
      case _ => PageType.UNCLASSIFIED
    }
  }

}
