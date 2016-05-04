package bot

import bot.utils.Classifier
import bot.wiki.{BotState, WikiImage, WikiPage}
import net.sourceforge.jwbf.core.contentRep.Article
import net.sourceforge.jwbf.mediawiki.actions.editing.FileUpload
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import net.sourceforge.jwbf.mediawiki.contentRep.SimpleFile
import spray.json._

import scala.collection.JavaConverters._

final class Bot(val url: String, val login: String, pass: String, val pageBot: String, val blacklist: Set[String]) {

  private val bot = new MediaWikiBot(url)
  private var _state = Bot.restoreState(bot.getArticle(pageBot))

  lazy val allRawPageTitles = new AllPageTitles(bot).iterator().asScala.toStream

  lazy val allPageTitles = allRawPageTitles.filter(!blacklist.contains(_))

  lazy val allWikiPages = allPageTitles.flatMap(load)

  def load(title: String): Option[WikiPage] =
    if (! allPageTitles.contains(title)) None
    else {
      log.debug("Loading {}", title)
      val article = bot.getArticle(title)
      val savedPage = _state.pages.find(_.title == title)

      savedPage match {
        case Some(page) if article.getRevisionId == page.revisionId =>
          log.debug("Unmodified page")
          savedPage

        case Some(modifiedPage) =>
          log.debug("Modified")

          if (Bot.imageTag.findFirstIn(article.getText).isEmpty) {

            _state = _state


          }
          article.getText match {
            case Bot.imageTag(urlOrigin, urlThumb, description) =>

          }

        case None =>
          log.debug("Unknown page")
          Some(
            new WikiPage(
              article.getTitle,
              article.getRevisionId,
              Classifier.findPageType(article.getTitle),
              Set.empty
            )
          )
      }
    }

  def state: BotState =
    _state

  def signIn(): Unit =
    bot.login(login, pass)

  def add(page: WikiPage, image: WikiImage): Unit =
    if (!page.images.contains(image)) {
      log.info("Adding {}", page.title)

      val load = bot.getPerformedAction(new FileUpload(new SimpleFile(page.title), bot))
      assert(load.hasMoreMessages, "upload failed")
      log.debug("Load result: {}", load.getNextMessage.getRequest)

      val article = bot.getArticle(page.title)
      val text = article.getText

      if (Bot.imageTag.findFirstMatchIn(text).isEmpty) {
        val pathOriginal = ""
        val pathThumb = ""
        val description = image.description
        val imageTag = s"""[[File:$pathOriginal|thumb=$pathThumb|alt=Alt|$description]]\n\n"""

        article.setText(imageTag + article.getText)
        article.save()
      }

      _state = _state.copy(pages = _state.pages - page + page.copy(images = page.images + image))
      save()
    }

  def remove(page: WikiPage): Unit =
    if (_state.pages.contains(page)) {
      log.info("Clearing {}", page.title)

      val article = bot.getArticle(page.title)
      val text = article.getText

      if (text.contains("[[File")) {
        article.setText(Bot.imageTag.replaceAllIn(text, "").trim)
        article.save()
      }

      _state = _state.copy(pages = _state.pages - page)
      save()
    }

  def save(): Unit = {
    log.debug("Saving bot state")

    val article = bot.getArticle(pageBot)
    val text = article.getText

    val startIdx = text.indexOf(Bot.startCacheTag)
    val endIdx = text.indexOf(Bot.endCacheTag)

    if (-1 < startIdx && -1 < endIdx) {
      assert(startIdx < endIdx, "invalid bot cache")
      article.setText(
        text.substring(0, startIdx) +
          "\n" +
          Bot.startCacheTag +
          _state.toJson.compactPrint +
          text.substring(endIdx)
      )
      log.debug("Bot state found and replaced")
    } else {
      article.setText(
        text +
          "\n" +
          Bot.startCacheTag +
          _state.toJson.compactPrint +
          Bot.endCacheTag
      )
      log.debug("Bot state not found, placing it")
    }
    article.save()
  }

}

object Bot {

  private val imageTag = "\\[\\[File:(.+)\\|thumb=(.+?)\\|(.+)\\]\\]".r

  val startCacheTag = "<!-----BOTCACHE=====!>"
  val endCacheTag = "<!=====ENDCACHE-----!>"

  def restoreState(article: Article): BotState = {
    log.debug("Loading bot state")

    val text = article.getText
    val startIdx = text.indexOf(startCacheTag)
    val endIdx = text.indexOf(endCacheTag)

    if (-1 < startIdx && -1 < endIdx) {
      assert(startIdx < endIdx, "invalid bot cache")
      log.debug("Bot state found")
      text.substring(startIdx + startCacheTag.length, endIdx).parseJson.convertTo[BotState]
    } else {
      log.debug("Bot state not found, creating empty one")
      BotState(article.getTitle, article.getRevisionId, Set.empty)
    }
  }

}
