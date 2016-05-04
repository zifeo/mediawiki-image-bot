package bot.providers

import java.io._
import java.net.{HttpURLConnection, URL}

import bot._
import bot.wiki.Image
import net.coobird.thumbnailator.Thumbnails

import scala.io.Source

object GoogleImages {

  private var idx = 0
  private val KEYS = config.getList("google.keys").toArray

  private val BASE_URL_1 = "https://www.googleapis.com/customsearch/v1?q="
  private val BASE_URL_2 = "&cx=005581394676374455442%3Afihmnxuedsw&hl=fr&num=5&rights=cc_attribute&searchType=image&key="

  private def googleRequest(link: String): String =
    Source.fromURL(BASE_URL_1 + link.replaceAll(" ", "+") + BASE_URL_2 + KEYS(idx)).mkString

  /*def parseRequest(page: String): List[Image] = {
    try {
      var content = googleRequest(page)
      if (content.isEmpty) {
        idx += 1
        content = googleRequest(page)
      }
      //content.parseJson.asJsObject
      var obj = new JSONObject(content)
      try {
        // obj.fields("error").asJsObject.fields("code").convertTo[String]
        if (obj.getJSONObject("error").getString("code") == "403") {
          idx += 1
          obj = new JSONObject(googleRequest(page))
        }
      } catch {
        case _: Exception =>
      }
      if (obj.getJSONObject("queries").getJSONArray("request").getJSONObject(0).getInt("totalResults") > 0) {
        val items = obj.getJSONArray("items")
        Stream.range(0, obj.getJSONObject("queries").getJSONArray("request").getJSONObject(0).getInt("count"))
          .map(i => items.getJSONObject(i)).map(j => new Image(page, j.getString("link"), j.getString("snippet")))
          .toList
      } else
        List()
    }
    catch {
      case e: Exception => if (idx < KEYS.length) {
        println(BASE_URL_1 + page.replaceAll(" ", "+") + BASE_URL_2 + KEYS(idx))
        e.printStackTrace()
        idx += 1
        parseRequest(page)
      } else List()
    }
  }

  def downloadImageFromURL(link: String, folder: String, name: String): Boolean = {
    try {
      var out: OutputStream = null
      var in: InputStream = null

      val url = new URL(link)

      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      in = connection.getInputStream
      val file = new File(folder)
      file.mkdirs
      out = new BufferedOutputStream(new FileOutputStream(name))
      val byteArray = Stream.continually(in.read).takeWhile(_ != -1).map(_.toByte).toArray

      out.write(byteArray)
      in.close()
      out.close()
      true
    }
    catch {
      case e: Exception => false
    }
  }

  // TODO : check
  def resize(from: String, to: String, size: Int) {
    try {
      val fileFrom = new File(from)
      val fileTo = new File(to)
      Thumbnails.of(fileFrom).size(size, size).outputFormat("jpg").toFile(fileTo)
    }
    catch {
      case e: Exception =>
    }
  }

  def safeString(raw: String) = "\"" + raw.replaceAll("\"", "\\\\\"") + "\""
  */

}
