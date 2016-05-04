package bot.utils

import bot.BotPage
import bot.wiki.{PageType, WikiImage, WikiPage}
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot
import org.json.{JSONArray, JSONObject}

object JSONParser {

  def parseBotData(bot: MediaWikiBot, raw: String): BotPage = {
    val json = new JSONObject(raw)
    val jsonPages = json.getJSONArray("pages")
    new BotPage(
      bot,
      json.getString("name"),
      json.getString("editor"),
      json.getLong("timestamp"),
      json.getString("revisionId"),
      json.getInt("totalPages"),
      Stream.range(0, jsonPages.length()).map(i => parsePage(jsonPages.getJSONObject(i))).toList)
  }

  private def parsePage(json: JSONObject): WikiPage = {

    val jsonImages = json.getJSONArray("images")

    val images = Stream.range(0, jsonImages.length()).map(i => parseImage(jsonImages.getJSONObject(i))).toList

    WikiPage(
      json.getString("title"),
      json.getString("revisionId"),
      json.getString("editor"),
      json.getString("editSummary"),
      PageType.UNCLASSIFIED /*PageType.withName(json.getString("pageType").toUpperCase)*/,
      toList(json.getJSONArray("keywords")),
      images,
      toList(json.getJSONArray("ignored")),
      json.getLong("timestamp")
    )
  }

  private def parseImage(raw: JSONObject): WikiImage =
    new WikiImage(raw.getString("snippet"), raw.getString("url"), raw.getString("thumbnail"), raw.getString("filename"))

  private def toList(array: JSONArray): List[String] =
    Stream.range(0, array.length()).map(i => array.getString(i)).toList

}