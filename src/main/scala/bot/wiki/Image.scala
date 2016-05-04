package bot.wiki

import java.io.File
import bot.providers.GoogleImages
import GoogleImages._
import bot.providers.GoogleImages

class Image(val page: String, val link: String, val snippet: String) {

  private val BASE_PATH = "download/"
  private val ORIG_SIZE = 640
  private val THUMB_SIZE = 222

  def saveToFile(): String = {
    if (link != null && !link.isEmpty) {
      val path = BASE_PATH + page + "/" + snippet.toLowerCase.replaceAll(" ", "_").replaceAll(".jpg", "").replaceAll("file:", "").replace('|', '-') + "_"
      val fileName = path + "original.jpg"
      val from = new File(fileName)
      val to = new File(path + "thumbnail.jpg")
      if (!from.exists() && !to.exists()) {
        //downloadImageFromURL(link, BASE_PATH + page + "/", fileName)
        //resize(fileName, from.getPath, ORIG_SIZE)
        //resize(fileName, to.getPath, THUMB_SIZE)
      }
      path
    } else {
      ""
    }
  }

  override def toString: String = {
    "Snippet : %s\nLink : %s".format(snippet, link)
  }

}
