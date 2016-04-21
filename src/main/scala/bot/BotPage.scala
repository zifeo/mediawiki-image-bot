package bot

import java.util.Calendar

import bot.JSONParser.parseBotData
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._

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

class BotPage(
               val bot: MediaWikiBot,
               val name: String,
               val editor: String,
               val timestamp: Long,
               val revisionId: String,
               var totalPages: Int,
               var pages: List[WikiPage]
             ) {

  def savePage(): Unit = {
    val article = bot.getArticle(BotPage.NAME)
    updateTotalPages()
    article.getText match {
      case BotPage.PAGE_REGEX(g1, g2, g3) => article.setText(g1 + toString + g2); article.save()
      case _ =>
    }
  }

  def updateTotalPages() = {
    totalPages = new AllPageTitles(bot).iterator().asScala.toList.length
  }

  override def toString: String = {
    ("{\n" +
      "\t\"name\" : \"%s\",\n" +
      "\t\"editor\" : \"%s\",\n" +
      "\t\"timestamp\" : %d,\n" +
      "\t\"revisionId\" : \"%s\",\n" +
      "\t\"totalPages\" : \"%d\",\n" +
      "\t\"pages\" : [\n%s" +
      "\t]\n" +
      "}\n"
      ).
      format(name, editor, timestamp, revisionId, totalPages, pages.mkString(", \n"))
  }

}
