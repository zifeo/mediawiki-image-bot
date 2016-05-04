package bot.wiki

import bot.providers.GoogleImages
import GoogleImages._
import bot.providers.GoogleImages

class WikiImage(
                 snippet: String,
                 url: String,
                 thumbnail: String,
                 filename: String
               ) {

  override def toString: String = {
    s"""
       |{
       |  "url": ${safeString(url)},
       |  "snippet": ${safeString(snippet)},
       |  "thumbnail": ${safeString(thumbnail)},
       |  "filename": ${safeString(filename)}
       |}
    """.stripMargin
  }

}
