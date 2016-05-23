package bot.scenarios

import java.io.File

import bot._
import bot.providers.{FlickrSearch, GoogleSearch}
import bot.utils.Tokenizer
import bot.wiki.{PageType, WikiImage, WikiPage}
import com.sun.prism.shader.AlphaOne_ImagePattern_AlphaTest_Loader

import scala.util.{Failure, Success, Try}

object Evaluation extends BotApp {

  bot.signIn()

  val allowedPageTypes = List(PageType.LITERAL)

  val images = bot
    .allWikiPages
    .filter(p => allowedPageTypes.contains(p.pageType))
    .map { page =>
      Try {
        val pageArticle = bot.getArticle(page.title)
        log.info("Page " + page.title)

        val yearsInPage = Tokenizer.year(pageArticle)

        val pageImage =
          if (yearsInPage.nonEmpty) {
            val average = yearsInPage.sum / yearsInPage.size

            if (average < config.getInt("yearThreshold")) {
              log.info(" search on Google : ")

              val res = GoogleSearch(page.title)
              if (res.isEmpty) {
                log.info("no results")
                FlickrSearch(page.title)
              } else {
                log.info("ok")
                res
              }

            } else {
              log.info(" search Flickr : ")
              val res = FlickrSearch(page.title)

              if (res.isEmpty) {
                log.info("no results")
                GoogleSearch(page.title)
              }
              else {
                log.info("ok")
                res
              }
            }

          } else {
            log.info(" search on Default : ")
            val res = GoogleSearch(page.title)

            if (res.isEmpty) {
              log.info("no results")
              FlickrSearch(page.title)
            } else {
              log.info("ok")
              res
            }
          }
        (page, pageImage)
      }
    }

  var img_count = 0
  images.foreach {
    case Failure(err) => log.warn("cannot process", err)
    case Success((page @ WikiPage(title, _, _, _), (_image, _file) #:: _)) =>
          bot.add(page, _image, _file)
          println("Add image for " + title + " with image " + _image.url)
          img_count += 1
          log.info(img_count + " Image added until now")

    case _ => log.warn("no action")
  }

  log.info("\n\n################### END #################")
  log.info(img_count + " Image added in total")
}
