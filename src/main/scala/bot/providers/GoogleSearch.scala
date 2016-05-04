package bot.providers

import java.io._
import java.net.{URL, URLEncoder}

import bot._
import bot.wiki.WikiImage
import spray.json._

import scala.collection.JavaConverters._
import scala.io.Source

object GoogleSearch {

  private val searchCount = 5

  private var keyIdx = 0
  private val keys = config.getStringList("google.keys").asScala.toArray

  private val apiUrl1 = "https://www.googleapis.com/customsearch/v1?q="
  private val apiUrl2 = "&cx=005581394676374455442%3Afihmnxuedsw&hl=fr&num="
  private val apiUrl3 = "&rights=cc_attribute&searchType=image&key="

  def apply(terms: String): List[(WikiImage, File)] = {
    log.info("Google searching for {}", terms)
    val res = call(terms)

    if (res.get("error").contains(JsString("403"))) {
      keyIdx += 1
      assert(keyIdx < keys.length, "no valid key found")
      apply(terms)
    } else {
      res("queries")
        .asJsObject
        .fields
        .get("items")
        .flatMap(_.convertTo[List[JsObject]])
        .toList
        .map { json =>

          val fields = json.fields
          val title = fields("snippet").convertTo[String]
          val link = fields("link").convertTo[String]
          val file = tempFileFromStream(new URL(link).openStream())

          log.debug("Found: {}", title)
          WikiImage(title, None, link, List.empty, None, "cc") -> file
        }
    }
  }

  private def call(query: String): Map[String, JsValue] =
    Source
      .fromURL(apiUrl1 + URLEncoder.encode(query, "UTF-8") + apiUrl2 + searchCount + apiUrl3 + keys(keyIdx))
      .mkString
      .parseJson
      .asJsObject
      .fields

}
