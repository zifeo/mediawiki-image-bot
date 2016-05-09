import java.io.{File, InputStream}
import java.nio.file.{StandardCopyOption, Files}
import java.util.logging.LogManager

import bot.wiki._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json._

package object bot {

  LogManager.getLogManager.readConfiguration()

  val config = ConfigFactory.load().getConfig("bot")
  val log = Logger(LoggerFactory.getLogger("bot"))

  def tempFileFromStream(stream: InputStream): File = {
    val file = File.createTempFile("mediawiki-image-bot-", ".jpg")
    file.deleteOnExit()
    Files.copy(stream, file.toPath, StandardCopyOption.REPLACE_EXISTING)
    stream.close()
    file
  }

  implicit object pageTypeFormat extends RootJsonFormat[PageType.PageType] {

    val mapping = PageType.values.map(v => v.toString -> v).toMap

    def write(obj: PageType.PageType): JsValue =
      JsString(obj.toString)

    def read(json: JsValue): PageType.PageType = json match {
      case JsString(str) if mapping.contains(str) => mapping(str)
      case _ => deserializationError("Not a PageType")
    }
  }

  implicit val wikiImageFormat: RootJsonFormat[WikiImage] = jsonFormat7(WikiImage)
  implicit val wikiPageFormat: RootJsonFormat[WikiPage] = jsonFormat4(WikiPage)
  implicit val botStateFormat: RootJsonFormat[BotState] = jsonFormat3(BotState.apply)

}
