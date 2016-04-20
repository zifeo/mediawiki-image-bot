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

}
