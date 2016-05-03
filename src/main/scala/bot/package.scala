import java.util.logging.LogManager

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

package object bot {

  LogManager.getLogManager.readConfiguration()

  val config = ConfigFactory.load().getConfig("bot")
  val log = Logger(LoggerFactory.getLogger("bot"))

}
