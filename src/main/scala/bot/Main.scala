package bot

import bot.Bot._
import BotPage.updateImages

object Main extends App {

  login()

  // This is needed to save the file every 25 uploads
  var i = 0
  while (i < allPages.length) {
    updateImages(i)
    i += 25
  }

  println("Done")

}
