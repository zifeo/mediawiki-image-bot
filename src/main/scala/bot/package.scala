import java.io.{File, InputStream}
import java.nio.file.{Files, StandardCopyOption}
import java.util.logging.LogManager

import bot.wiki._
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.PngWriter
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
import spray.json._

package object bot {

  LogManager.getLogManager.readConfiguration()

  val config = ConfigFactory.load().getConfig("bot")
  val log = Logger(LoggerFactory.getLogger("bot"))

  val maxSizeImage = config.getDouble("maxImageSize")
  implicit val writer = PngWriter.MinCompression

  def cleanName(raw: String): String =
    raw
      .trim
      .replaceAll(".svg", "")
      .replaceAll(".jpg", "")
      .replaceAll(".jpeg", "")
      .replaceAll(".png", "")
      .replaceAll(".gif", "")
      .replaceAll("File:", "")
      .replaceAll("file:", "")
      .replaceAll("Ficher:", "")
      .replaceAll("fichier:", "")

  def tempFileFromStream(stream: InputStream): File = {
    val file = File.createTempFile("mediawiki-image-bot-", "")
    file.deleteOnExit()

    val image = Image.fromStream(stream)
    val (width, height) = image.dimensions
    log.info(s"image size: ${width}x$height")
    val scaleFactor = (maxSizeImage / width.max(height)).min(1)
    if (scaleFactor < 1) {
      log.info("scale factor: {}", scaleFactor)
    }
    val resized = image.resize(scaleFactor).stream

    Files.copy(resized, file.toPath, StandardCopyOption.REPLACE_EXISTING)
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
