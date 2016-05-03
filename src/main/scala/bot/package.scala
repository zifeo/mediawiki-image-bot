import java.util.logging.LogManager

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

package object bot {

  LogManager.getLogManager.readConfiguration()

  val config = ConfigFactory.load().getConfig("bot")
  val log = LoggerFactory.getLogger("bot")

}
