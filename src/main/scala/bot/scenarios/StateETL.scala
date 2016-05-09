package bot.scenarios

import bot._

object StateETL extends BotApp {

  println(bot.state)

  bot.signIn()
  bot.save()

}
