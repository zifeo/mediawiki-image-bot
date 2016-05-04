package bot.scenarios

import bot._
import bot.providers.FlickrSearch

object BotASable extends BotApp {

  bot.signIn()
  val p = bot.load("BotASable")

  val res = FlickrSearch("Matrix")

  bot.add(p.get, res.head._1, res.head._2)

  println(res)



}
