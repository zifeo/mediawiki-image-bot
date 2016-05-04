package bot.wiki

import bot._
import net.sourceforge.jwbf.core.contentRep.Article
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import spray.json._

case class BotState (
                      botPage: String,
                      revisionId: String,
                      pages: Set[WikiPage]
                    ) {

  def save(bot: MediaWikiBot): Unit = {
    log.debug("Saving bot state")

    val article = bot.getArticle(botPage)
    val text = article.getText
    val startIdx = text.indexOf(BotState.startCacheTag)
    val endIdx = text.indexOf(BotState.endCacheTag)

    if (-1 < startIdx && -1 < endIdx) {
      assert(startIdx < endIdx, "invalid bot cache")
      article.setText(
        text.substring(0, startIdx) +
          "\n" +
          BotState.startCacheTag +
          this.toJson.compactPrint +
          text.substring(endIdx)
      )
      log.debug("Bot state found and replaced")
    } else {
      article.setText(
        text +
          "\n" +
          BotState.startCacheTag +
          this.toJson.compactPrint +
          BotState.endCacheTag
      )
      log.debug("Bot state not found, placing it")
    }
    article.save()
  }

  // TODO
  def removeFileInArticles(exceptions: List[String]): BotState = {
    val pages = this.pages.filter(p => !exceptions.contains(p.title))
    this.copy(pages = pages)
  }

}

object BotState {

  val startCacheTag = "<!-----BOTCACHE=====!>"
  val endCacheTag = "<!=====ENDCACHE-----!>"

  def parse(article: Article): BotState = {
    log.debug("Loading bot state")

    val text = article.getText
    val startIdx = text.indexOf(BotState.startCacheTag)
    val endIdx = text.indexOf(BotState.endCacheTag)

    if (-1 < startIdx && -1 < endIdx) {
      assert(startIdx < endIdx, "invalid bot cache")
      log.debug("Bot state found")
      text.substring(startIdx + BotState.startCacheTag.length, endIdx).parseJson.convertTo[BotState]
    } else {
      log.debug("Bot state not found, creating empty one")
      BotState(article.getTitle, article.getRevisionId, Set.empty)
    }
  }

}
