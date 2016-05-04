package bot

import scala.collection.JavaConverters._

trait BotApp extends App {

  val bot = new Bot(
    config.getString("mediawiki"),
    config.getString("login"),
    config.getString("password"),
    config.getStringList("blacklist").asScala.toList
  )

}
