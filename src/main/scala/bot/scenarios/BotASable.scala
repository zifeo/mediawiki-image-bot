package bot.scenarios

import bot.BotApp
import bot.providers.{GoogleSearch, FlickrSearch}

object BotASable extends BotApp {

  bot.signIn()

  val Some(page) = bot.load("BotASable")
  val terms = "Chrétiens sociaux"

  val search = GoogleSearch(terms)
  println(search.toList)
  val (image, file) #:: _ = search

  bot.add(page, image, file)

}
