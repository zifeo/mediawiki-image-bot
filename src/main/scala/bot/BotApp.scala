package bot

import scala.collection.JavaConverters._

trait BotApp extends App {

  log.info("Bot ready")

  val bot = new Bot(
    config.getString("mediawiki"),
    config.getString("login"),
    config.getString("password"),
    config.getString("botPage"),
    config.getStringList("blacklist").asScala.toSet
  )

}
