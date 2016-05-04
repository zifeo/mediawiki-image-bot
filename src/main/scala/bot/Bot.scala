package bot

import bot.wiki.WikiPage
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._

final class Bot(url: String, login: String, pass: String, blacklist: List[String]) {

  private lazy val bot = new MediaWikiBot(url)

  lazy val allPageTitles = new AllPageTitles(bot).iterator().asScala.toList

  lazy val allWikiPages = allPageTitles.map(p => new WikiPage(bot.getArticle(p)))

  lazy val allLiteralPages = allWikiPages

  def signIn(): Unit =
    bot.login(login, pass)




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
