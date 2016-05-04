package bot.scenarios

import bot._

object ShowState extends BotApp {

  println(bot.state)

  bot.signIn()
  bot.saveState()

}
