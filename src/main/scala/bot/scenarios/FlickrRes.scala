package bot.scenarios

import bot.BotApp
import bot.providers.FlickrSearch

object FlickrRes extends BotApp {

  val epfl = FlickrSearch("EPFL").mkString("\n")
  println(epfl)

}
