package bot

import bot.Bot._
import BotPage.updateImages

object Main extends App {

  //login()

  //allPages.slice(525, 550).foreach(removeFileFromArticle(_))

  // This is needed to save the file every 25 uploads
  var i = 700

  while (i < allPages.length) {
    updateImages(i)
    i += 25
  }

  BotPage.getPageFromArticle(bot).withTotalPages().savePage()

  //println(allPages.indexOf("Nation Suisse"))

  println("Done")


}
