package bot

import bot.IOUtils._

class Image(val page: String, val link: String, val snippet: String) {

  private val BASE_PATH = "download/"
  private val ORIG_SIZE = 640
  private val THUMB_SIZE = 222

  def saveToFile(): String = {
    if (link != null && !link.isEmpty) {
      val path = BASE_PATH + page + "/" + snippet.toLowerCase.replaceAll(" ", "_").replaceAll(".jpg", "").replaceAll("file:", "") + "_"
      val fileName = path + "original.jpg"
      downloadImageFromURL(link, BASE_PATH + page + "/", fileName)
      resize(fileName, fileName, ORIG_SIZE)
      resize(fileName, path + "thumbnail.jpg", THUMB_SIZE)
      path
    } else {
      ""
    }
  }

  override def toString: String = {
    "Snippet : %s\nLink : %s".format(snippet, link)
  }
}
