package bot.scenarios

import java.io.File

import bot._
import bot.providers.{FlickrSearch, GoogleSearch}
import bot.utils.Tokenizer
import bot.wiki.{WikiImage, WikiPage, PageType}
import com.sun.prism.shader.AlphaOne_ImagePattern_AlphaTest_Loader

object Evaluation extends BotApp {

  bot.signIn()

  val allowedPageTypes = List(PageType.UNCLASSIFIED, PageType.LITERAL)

  val images = bot
    .allWikiPages
    .filter(p => allowedPageTypes.contains(p.pageType))
    .map { page =>

      val pageArticle = bot.getArticle(page.title)
      print("Page " + page.title)

      val yearsInPage = Tokenizer.year(pageArticle)

      val pageImage =
        if (yearsInPage.nonEmpty) {
          val average = yearsInPage.sum / yearsInPage.size

          if (average < config.getInt("yearThreshold")) {
            print(" search on Google : ")

            val res = GoogleSearch(page.title)
            if (res.isEmpty) {
              println("no results")
              FlickrSearch(page.title)
            } else {
              println("ok")
              res
            }

          } else {
            print(" search Flickr : ")
            val res = FlickrSearch(page.title)

            if (res.isEmpty){
              println("no results")
              GoogleSearch(page.title)
            }
            else {
              println("ok")
              res
            }
          }

        } else {
          print(" search on Default : ")
          val res = GoogleSearch(page.title)

          if (res.isEmpty) {
            println("no results")
            FlickrSearch(page.title)
          } else {
            println("ok")
            res
          }
        }
      (page, pageImage)
    }

  images.take(5).foreach {
    //case (_, Stream.empty) =>
    case (page, im) =>
      if (im.nonEmpty) {
        val (_image, _file) = im.head
        bot.add(page, _image, _file)
        println("Add image for " + page.title + " with image " + _image.url)
      }
  }

}
