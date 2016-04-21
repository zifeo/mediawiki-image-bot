package bot

import bot.IOUtils._
import bot.Bot._

import scala.util.Try

object Main extends App {

  login()

  var botPage = BotPage.getPageFromArticle(bot)
      .withTotalPages()
    .withPages(allLiteralPages.take(5)
      .map(t => new WikiPage(bot.getArticle(t))
        .withKeywords()
        .withImages())
    )

  println(botPage)


  println("Done")

  //val name = "several_s-bots_in_swarm-bot_original.jpg"
  //val file = "/Users/asoccard/Developer/Scala/mediawiki-image-bot/download1/Tour Eiffel/tour_eiffel_thumbnail.jpg"
  //bot.getPerformedAction(new FileUpload(new SimpleFile(file), bot))
  //article.save()

}
