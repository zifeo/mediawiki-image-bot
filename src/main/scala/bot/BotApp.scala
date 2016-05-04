package bot

import scala.collection.JavaConverters._

trait BotApp extends App {

  log.info("Bot ready")

  val bot = new Bot(
    config.getString("mediawiki"),
    config.getString("login"),
    config.getString("password"),
    config.getStringList("blacklist").asScala.toList
  )

}
