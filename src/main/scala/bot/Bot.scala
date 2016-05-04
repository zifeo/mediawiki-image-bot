package bot

import bot.wiki.{BotState, WikiPage}
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._

final class Bot(val url: String, val login: String, pass: String, val pageBot: String, val blacklist: Set[String]) {

  private val bot = new MediaWikiBot(url)
  private var _state = BotState.parse(bot.getArticle(pageBot))

  lazy val allRawPageTitles = new AllPageTitles(bot).iterator().asScala.toStream

  lazy val allPageTitles = allRawPageTitles.filter(!blacklist.contains(_))

  lazy val allWikiPages = allPageTitles.map { title =>
    log.debug("Loading {}", title)
    new WikiPage(bot.getArticle(title))
  }

  lazy val allLiteralPages = allWikiPages

  def signIn(): Unit =
    bot.login(login, pass)

  def saveState(): Unit =
    _state.save(bot)

  def state: BotState =
    _state


}

object Bot {

  val startCacheTag = "<=====START==CACHE=====>"
  val endCacheTag = "<=====END==CACHE=====>"

}
