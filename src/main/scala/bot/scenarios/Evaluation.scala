package bot.scenarios

import bot._
import bot.providers.{FlickrSearch, GoogleSearch}
import bot.utils.Tokenizer
import bot.wiki.PageType

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
      (page.title, pageImage)
    }

  val results = images.take(20).toList
  results.foreach { res =>
    print(res._1 + " : ")
    res._2.foreach(im => print(im._1.url + ",  "))
    println("\n\n")
  }

  /*
  println("Flickr result : ")
  resFlickr.foreach(_._1.print())
  println("Google result : ")
  resGoogle.foreach(_._1.print())

  println("\n\nTotal resutlts :")
  println("\tFilckr : " + resFlickr.size)
  println("\tGoogle : " + resGoogle.size)
  */

}
