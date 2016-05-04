package bot.wiki

import bot.utils.Classifier
import bot.wiki.PageType.PageType
import net.sourceforge.jwbf.core.contentRep.Article
import spray.json._
import bot._

case class WikiPage(
                      title: String,
                      revisionId: String,
                      pageType: PageType,
                      images: List[WikiImage]
                    ) {

  def this(article: Article) {
    this(
      article.getTitle,
      article.getRevisionId,
      Classifier.findPageType(article.getTitle),
      List.empty
    )
  }

  /*private def clearString(raw: String): String = raw.replaceAll(".jpg", "").replaceAll(".png", "").replaceAll(".gif", "").replaceAll("fichier:", "").replaceAll("Fichier:", "").replaceAll("file:", "").replaceAll("File:", "")


  def isMaxImageReached: Boolean = ignoredImages.length > 5

  private val REMOVE_FILE_REGEX = "\\[\\[File:(.+)\\|thumb=(.+?)\\|(.+)\\]\\]"

  def removeFileFromArticle(title: String) {
    println(title)
    val txt = article.getText
    if (txt.contains("[[File")) {
      try {
        article.setText(txt.replaceAll(REMOVE_FILE_REGEX, "").trim)
        article.save()
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  def withKeywords: WikiPage = {
    //this.copy(keywords = (keywords ++ Tokenizer.hyperwordTokenizer(Bot.bot.getArticle(title))).distinct)
    this
  }

  def withIgnored(url: String): WikiPage = {
    this.copy(ignoredImages = url :: ignoredImages, images = images.filter(i => i.url != url))
  }

  def withImages(): WikiPage = {
    val images = IO.parseRequest(title)

    val image = images.find(img => !ignoredImages.contains(img.link))

    println(title)

    if (image.isDefined) {
      val path = image.get.saveToFile()
      val paths = List("original.jpg", "thumbnail.jpg").map(path + _)

      try {
          paths.foreach(p => bot.getPerformedAction(new FileUpload(new SimpleFile(p), bot)))
        val article = bot.getArticle(title)

        val originalPath = paths.head.substring(paths.head.lastIndexOf("/") + 1)
        val thumbnailPath = paths(1).substring(paths(1).lastIndexOf("/") + 1)
        val snippet = clearString(image.get.snippet)

        val wikiFile =
          s"""[[File:$originalPath|thumb=$thumbnailPath|alt=Alt|$snippet]]\n\n"""

        if (!article.getText.contains(wikiFile)) {
          article.setText(wikiFile + article.getText)
            article.save()

          val wikiImage = new WikiImage(snippet, image.get.link, originalPath, thumbnailPath)
          this.copy(images = wikiImage :: this.images)
        } else {
          this
        }
      } catch {
        case e: Exception => this
      }
    } else {
      println("ERREUR")
      this
    }
  }*/

}
