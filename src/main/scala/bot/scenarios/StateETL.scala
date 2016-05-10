package bot.scenarios

import bot._

object StateETL extends BotApp {

  println(bot.state)

  val page :: _ = bot.state.pages.filter(_.title == "BotASable").toList
  println(page.images.mkString("\n"))

  bot.signIn()
  bot.save()

}
