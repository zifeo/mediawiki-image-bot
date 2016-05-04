package bot.scenarios

import bot._
import bot.utils.Classifier

object ClassifyPages extends BotApp {

  bot
    .allPageTitles
    .groupBy(Classifier.findPageType)
    .foreach { case (pageType, pages) =>

      val pageList = pages.mkString("\n  - ", "\n  - ", "")
      println(s"$pageType:$pageList")

    }

  val blacklisted = bot.allRawPageTitles.filter(bot.blacklist.contains).mkString("\n  - ", "\n  - ", "")
  println(s"Blacklisted:$blacklisted")

}
