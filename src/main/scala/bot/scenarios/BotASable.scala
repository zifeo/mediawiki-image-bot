package bot.scenarios

import bot.BotApp
import bot.providers.GoogleSearch

object BotASable extends BotApp {

  bot.signIn()

  val Some(page) = bot.load("BotASable")
  val terms = "Herbert George Wells"
  //bot.remove(page)

  val search = GoogleSearch(terms)
  val (image, file) = search.head

  //bot.add(page, image, file)

}
