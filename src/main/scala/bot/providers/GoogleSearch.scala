package bot.providers

import java.io._
import java.net.{URL, URLEncoder}

import bot._
import bot.wiki.WikiImage
import spray.json.DefaultJsonProtocol._
import spray.json._

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.{Success, Try}

object GoogleSearch {

  private val searchCount = 5

  private var keyIdx = 0
  private val keys = loadGoogleKeys().toArray

  private val apiUrl1 = "https://www.googleapis.com/customsearch/v1?q="
  private val apiUrl2 = "&cx=005581394676374455442%3Afihmnxuedsw&hl=fr&num="
  private val apiUrl3 = "&rights=cc_attribute&searchType=image&key="

  def loadGoogleKeys() : List[String] = {
    val filename = config.getString("googleKeysFile")
    def inner(lines : Iterator[String], acc : List[String]) : List[String] = {
      if (!lines.hasNext) acc
      else inner(lines, lines.next() :: acc)
    }
    inner(Source.fromFile(filename).getLines(), List())
  }

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
        .flatMap { json =>

          val fields = json.fields
          val name = cleanName(fields("snippet").convertTo[String]).trim
          val description = fields.get("title").map(x => cleanName(x.convertTo[String]).take(200).trim).getOrElse(name)
          val link = fields("link").convertTo[String]
          Try(new URL(link).openStream()) match {
            case Success(str) if str.available() > 0 =>
                val file = tempFileFromStream(str)
                val ext = link.reverse.takeWhile(_ != '.').reverse.toLowerCase

                log.debug("Found: {}", fields("snippet"))
                Option(WikiImage(s"$name.png", None, link, List.empty, description, "cc") -> file)
            case _ =>  None
          }
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
