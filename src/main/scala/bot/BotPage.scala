package bot

import java.util.Calendar

import bot.utils.JSONParser
import bot.utils.IO._
import JSONParser.parseBotData
import bot.wiki.WikiPage
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import bot.Bot._

case class BotPage(
                    bot: MediaWikiBot,
                    name: String,
                    editor: String,
                    timestamp: Long,
                    revisionId: String,
                    totalPages: Int,
                    pages: List[WikiPage]
                  ) {

  def savePage(): Unit = {
    println("\t\t\tSAVE PAGE")
    val article = bot.getArticle(BotPage.NAME)
    val text = article.getText
    val startIdx = text.indexOf(BotPage.START_CACHE)
    if (startIdx > -1) {
      val endIdx = text.indexOf(BotPage.END_CACHE)
      if (endIdx > startIdx) {
        try {
          article.setText(text.substring(0, startIdx) + "\n" + BotPage.START_CACHE + toString + text.substring(endIdx))
          article.save()
        } catch {
          case _: Exception => println("Error")
        }
      }
    }
  }

  def withPages(pages: List[WikiPage]): BotPage =
    this.copy(pages = pages ::: this.pages)

  def withTotalPages: BotPage =
    this.copy(totalPages = allPages.length)

  def removeFileInArticles(exceptions: List[String]): BotPage = {
    val pages = this.pages.filter(p => !exceptions.contains(p.title))
    this.copy(pages = pages)
  }

  override def toString: String = {
    s"""
       |{
       | "name": ${safeString(name)},
       | "editor": ${safeString(editor)},
       | "timestamp" : $timestamp,
       | "revisionId" : ${safeString(revisionId)},
       | "totalPages" : $totalPages,
       | "pages" : [${pages.sortBy(_.title).mkString(",")}  ]
       |}
    """.stripMargin
  }

}

object BotPage {

  val NAME = "ImageBot"

  val START_CACHE = "<=====START==CACHE=====>"
  val END_CACHE = "<=====END==CACHE=====>"

  def updateImages(idx: Int): Unit = {
    val botPage = BotPage
      .getPageFromArticle(bot)
      .withTotalPages
      .withPages {
        allPages
          .slice(idx, idx + 25)
          .map { t =>
            new WikiPage(bot.getArticle(t))
              .withKeywords
              .withImages()
          }
      }
    botPage.savePage()
  }

  def getPageFromArticle(bot: MediaWikiBot): BotPage = {
    val article = bot.getArticle(NAME)
    val text = article.getText
    val startIdx = text.indexOf(START_CACHE)

    if (startIdx > -1) {
      val endIdx = text.indexOf(END_CACHE)
      if (endIdx > startIdx) {
        try {
          parseBotData(bot, text.substring(startIdx + START_CACHE.length, endIdx).trim)
        } catch {
          case e: Exception =>
            new BotPage(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
        }
      } else {
        new BotPage(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
      }
    } else {
      new BotPage(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
    }
  }

}
