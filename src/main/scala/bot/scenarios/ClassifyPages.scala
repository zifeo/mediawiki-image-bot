package bot.scenarios

import bot._
import bot.wiki.WikiPage

object ClassifyPages extends BotApp {

  bot
    .allPageTitles
    .groupBy(WikiPage.findPageType)
    .foreach { case (pageType, pages) =>

      val pageList = pages.mkString("\n  - ")
      println(s"$pageType:\n  - $pageList")

    }

}
