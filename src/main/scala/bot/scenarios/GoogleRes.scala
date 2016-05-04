package bot.scenarios

import bot.BotApp
import bot.providers.GoogleSearch

object GoogleRes extends BotApp {

  val epfl = GoogleSearch("EPFL").mkString("\n")
  println(epfl)

}
