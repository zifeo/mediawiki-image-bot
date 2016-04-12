package bot

import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

object Main extends App {

  println("hello")
  val bot = new MediaWikiBot(config.getString("mediawiki"))

  println(bot.getSiteinfo)

  val article = bot.getArticle("Bots")
  println(article.getText)
  //bot.login(config.getString("login"), config.getString("password"))
  //article.save()

}
