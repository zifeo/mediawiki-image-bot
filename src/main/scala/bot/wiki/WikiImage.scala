package bot.wiki

import bot.utils.IO
import IO._

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
