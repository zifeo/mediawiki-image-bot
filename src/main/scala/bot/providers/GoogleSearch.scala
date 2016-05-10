package bot.providers

import java.io._
import java.net.{URLDecoder, URL, URLEncoder}
import java.nio.file.Files

import bot._
import bot.wiki.WikiImage
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConverters._
import scala.io.Source

object GoogleSearch {

  private val searchCount = 5

  private var keyIdx = 0
  private val keys = config.getStringList("google.keys").asScala.toArray

  private val apiUrl1 = "https://www.googleapis.com/customsearch/v1?q="
  private val apiUrl2 = "&cx=005581394676374455442%3Afihmnxuedsw&hl=fr&num="
  private val apiUrl3 = "&rights=cc_attribute&searchType=image&key="

  def apply(terms: String): Stream[(WikiImage, File)] = {
    log.info("Google searching for {}", terms)
    val res = call(terms)

    if (res.get("error").contains(JsString("403"))) {
      keyIdx += 1
      assert(keyIdx < keys.length, "no valid key found")
      apply(terms)
    } else if (!res.contains("items")) {
      log.warn("Google failed to return items: {}", res)
      Stream.empty
    } else {
      res("items")
        .convertTo[List[JsObject]].toStream
        .map { json =>

          val fields = json.fields
          val name = fields("snippet")
            .convertTo[String]
            .take(50)
            .trim
            .replaceAll(".svg", "")
            .replaceAll(".jpg", "")
            .replaceAll(".jpeg", "")
            .replaceAll(".png", "")
            .replaceAll(".gif", "")
            .replaceAll("File:", "")
            .replaceAll("file:", "")
          val link = fields("link").convertTo[String]
          val file = tempFileFromStream(new URL(link).openStream())
          val ext = link.reverse.takeWhile(_ != '.').reverse.toLowerCase

          log.debug("Found: {}", name)
          WikiImage(s"$name.png", None, link, List.empty, name, "cc") -> file
        }
    }
  }

  private def call(query: String): Map[String, JsValue] =
  try {
    Source
      .fromURL(apiUrl1 + URLEncoder.encode(query, "UTF-8") + apiUrl2 + searchCount + apiUrl3 + keys(keyIdx))
      .mkString
      .parseJson
      .asJsObject
      .fields
  }
  catch {
    case _ : Exception =>
      keyIdx += 1
      assert(keyIdx < keys.length, "no valid key found")
      call(query)
  }

}
