package bot

import bot.IOUtils._

class WikiImage(
                 val snippet: String,
                 val url: String,
                 val thumbnail: String,
                 var filename: String
               ) {

  override def toString: String = {
    s"""
       |        {
       |          "url": ${safeString(url)},
       |          "snippet": ${safeString(snippet)},
       |          "thumbnail": ${safeString(thumbnail)},
       |          "filename": ${safeString(filename)}
       |        }
    """.stripMargin
  }

}
