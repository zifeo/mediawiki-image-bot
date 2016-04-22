package bot

import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._

object Bot {

  private val atLeastOneChar = """[a-zA-Z]""".r

  val bot = new MediaWikiBot(config.getString("mediawiki"))

  val allPages = new AllPageTitles(bot).iterator().asScala.toList
  val allLiteralPages = allPages.filter(p => atLeastOneChar.findFirstIn(p).isDefined)

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
