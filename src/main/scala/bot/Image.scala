package bot

import bot.IOUtils._

class Image(val page: String, val orig: String, val thumb: String, val snippet: String) {

  private val BASE_PATH = "download1/"

  def this(page: String, orig: String, snippet: String) {
    this(page, orig, "", snippet)
  }

  def saveToFile(): Unit = {
    saveImageToFile(orig, true)
    saveImageToFile(thumb, false)
  }

  private def saveImageToFile(s: String, orig: Boolean) {
    if (s != null) {
      val fileName = BASE_PATH + page + "/" + snippet.toLowerCase.replaceAll(" ", "_") + "_" + (if (orig) "original"
      else "thumbnail") + ".jpg"
      downloadImageFromURL(s, BASE_PATH + page + "/", fileName)
      if (orig) resize(fileName)
    }
  }

  override def toString: String = {
    String.format("Snippet : %s\nLink : %s\nThumbnail : %s", snippet, orig, thumb)
  }
}
