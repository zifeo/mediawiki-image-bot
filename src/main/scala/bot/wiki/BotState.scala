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

  /*
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

  def getPageFromArticle(bot: MediaWikiBot): BotState = {
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
            new BotState(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
        }
      } else {
        new BotState(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
      }
    } else {
      new BotState(bot, NAME, article.getEditor, Calendar.getInstance().getTimeInMillis, article.getRevisionId, 0, List())
    }
  }

  def parseBotData(bot: MediaWikiBot, raw: String): BotState = {
    val json = new JSONObject(raw)
    val jsonPages = json.getJSONArray("pages")
    new BotState(
      bot,
      json.getString("name"),
      json.getString("editor"),
      json.getLong("timestamp"),
      json.getString("revisionId"),
      json.getInt("totalPages"),
      Stream.range(0, jsonPages.length()).map(i => parsePage(jsonPages.getJSONObject(i))).toList)
  }

  private def parsePage(json: JSONObject): WikiPage = {

    val jsonImages = json.getJSONArray("images")

    val images = Stream.range(0, jsonImages.length()).map(i => parseImage(jsonImages.getJSONObject(i))).toList

    WikiPage(
      json.getString("title"),
      json.getString("revisionId"),
      json.getString("editor"),
      json.getString("editSummary"),
      PageType.UNCLASSIFIED /*PageType.withName(json.getString("pageType").toUpperCase)*/,
      toList(json.getJSONArray("keywords")),
      images,
      toList(json.getJSONArray("ignored")),
      json.getLong("timestamp")
    )
  }

  private def parseImage(raw: JSONObject): WikiImage =
    new WikiImage(raw.getString("snippet"), raw.getString("url"), raw.getString("thumbnail"), raw.getString("filename"))
*/

}
