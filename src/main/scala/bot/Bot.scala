package bot

import java.io.File

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

  /** Loads given title as a page and check state integrity. */
  def load(title: String): Option[WikiPage] =
    if (! allPageTitles.contains(title)) None
    else {
      log.debug("Loading {}", title)
      val article = bot.getArticle(title)
      val statePage = _state.pages.find(_.title == title)

      val res = statePage match {
        case Some(page) if article.getRevisionId == page.revisionId =>
          log.debug("Unmodified page")
          page

        case Some(modifiedPage) if Bot.imageTag.findFirstIn(article.getText).isEmpty =>
          log.debug("Modified empty page")

          // images are manipulated always in order
          val (discarded, available) = modifiedPage.images.span(_.discarded)

          val newImages = available match {
            case Nil => // all tries discarded
              discarded

            case current :: Nil => // last try discarded
              discarded ::: List(current.copy(discarded = true))

            case current :: next :: tail => // at least only try available
              Bot.removeImageTag(article, current)
              Bot.addImageTag(article, next)
              discarded ::: List(current.copy(discarded = true), next) ::: tail
          }

          val newPage = modifiedPage.copy(images = newImages, revisionId = article.getRevisionId)
          _state = _state.copy(pages = _state.pages - modifiedPage + newPage)
          save()
          newPage

        case Some(modifiedPage) =>
          log.debug("Modified page with image")

          val newPage = modifiedPage.copy(revisionId = article.getRevisionId)
          _state = _state.copy(pages = _state.pages - modifiedPage + newPage)
          save()
          newPage

        case None =>
          log.debug("Unknown page")

          new WikiPage(
            article.getTitle,
            article.getRevisionId,
            Classifier.findPageType(article.getTitle),
            List.empty
          )
      }
      Some(res)
    }

  def state: BotState =
    _state

  def signIn(): Unit =
    bot.login(login, pass)

  /** Adds an image and the page in state (if not already present). */
  def add(page: WikiPage, image: WikiImage, file: File): Unit =
    if (!page.images.contains(image)) {
      log.info("Adding {}", page.title)

      bot.getPerformedAction(new FileUpload(new SimpleFile(image.filename, file), bot))

      val article = bot.getArticle(page.title)
      Bot.addImageTag(article, image)

      val newPage = page.copy(images = page.images ::: List(image))
      _state = _state.copy(pages = _state.pages - page + newPage)
      save()
    }

  /** Removes current image (if set by the bot) and given page from bot state. */
  def remove(page: WikiPage): Unit =
    if (_state.pages.contains(page)) {
      log.info("Clearing {}", page.title)

      val article = bot.getArticle(page.title)
      page.images.find(! _.discarded).foreach { image =>
        Bot.removeImageTag(article, image)
      }

      _state = _state.copy(pages = _state.pages - page)
      save()
    }

  /** Saves bot state. */
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

  private val imageTag = "\\[\\[File:(.+)\\|(.+)\\|(.+)\\]\\]".r

  val startCacheTag = "<!-----BOTCACHE=====!>"
  val endCacheTag = "<!=====ENDCACHE-----!>"

  /* Retrieves bot state. */
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

  /** Adds given image in article. */
  private def addImageTag(article: Article, image: WikiImage): Unit =
    if (imageTag.findFirstMatchIn(article.getText).isEmpty) {
      val filename = image.filename
      val description = image.description
      val imageTag = s"""[[File:$filename|thumb|200x200px|upright|$description]]\n"""
      article.setText(imageTag + article.getText)
      article.save()
    }

  /** Removes given image from article. */
  private def removeImageTag(article: Article, image: WikiImage): Unit = {
    val text = article.getText
    val url = image.url
    text match {
      case imageTag(`url`, _, _) => // image set by the bot
        article.setText(imageTag.replaceAllIn(text, "").trim)
        article.save()
      case _ => // other image
    }
  }

}
