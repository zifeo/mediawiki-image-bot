package bot

import java.util.Calendar

import bot.IOUtils._
import bot.JSONParser.parseBotData
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import bot.Bot._

object BotPage {
  val PAGE_REGEX = "((?:.|\\n)*)<=====START==CACHE=====>((?:.|\\n)*)<=====END==CACHE=====>((?:.|\\n)*)".r
  val NAME = "ImageBot"

  def getPageFromArticle(bot: MediaWikiBot): BotPage = {
    val article = bot.getArticle(NAME)
    article.getText match {
      case PAGE_REGEX(_, g, _) =>
        try {
          parseBotData(bot, g)
        } catch {
          case _: Exception => new BotPage(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
        }
      case _ => new BotPage(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
    }
  }

}

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
    val article = bot.getArticle(BotPage.NAME)
    article.getText match {
      case BotPage.PAGE_REGEX(g1, g2, g3) => article.setText(g1 + toString + g2); article.save()
      case _ =>
    }
  }

  def withPages(pages: List[WikiPage]) = this.copy(pages = pages)

  def withTotalPages() = this.copy(totalPages = allPages.length)

  override def toString: String = {
    s"""
       |{
       | "name": ${safeString(name)},
       | "editor": ${safeString(editor)},
       | "timestamp" : $timestamp,
       | "revisionId" : ${safeString(revisionId)},
       | "totalPages" : $totalPages,
       | "pages" : [
       |${pages.mkString(", \n")}
       |  ]
       |}
    """.stripMargin
  }

}
