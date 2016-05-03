package bot

import bot.utils.Regex
import bot.wiki.{PageType, WikiPage}
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._

object Bot {

  val bot = new MediaWikiBot(config.getString("mediawiki"))

  val blacklist = config.getList("blacklist")

  val allPages = new AllPageTitles(bot).iterator().asScala.toList.filter(p => !blacklist.contains(p))
  val allLiteralPages = allPages.filter(p => Regex.atLeastOneChar.findFirstIn(p).isDefined)

  def login() = bot.login(config.getString("login"), config.getString("password"))

  def traverAllPages() = {
    allPages.filter(p => WikiPage.getTypeOfArticle(p) == PageType.NONE).foreach(p => {
      println(p)
    })
  }

  private val REMOVE_FILE_REGEX = "\\[\\[File:(.+)\\|thumb=(.+?)\\|(.+)\\]\\]"

  def removeFileFromArticle(title: String) {
    println(title)
    val article = bot.getArticle(title)
    val txt = article.getText
    if (txt.contains("[[File")) {
      try {
        article.setText(txt.replaceAll(REMOVE_FILE_REGEX, "").trim)
        article.save()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

}
