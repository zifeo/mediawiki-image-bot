package bot

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import com.flickr4java.flickr.photos.{Photo, SearchParameters, Size}
import com.flickr4java.flickr.{Flickr, REST}
import net.sourceforge.jwbf.core.contentRep.Article
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._
import scala.util.matching.Regex

object Main extends App {

  val atLeastOneChar = """[a-zA-Z]""".r
  val hyperwordSelector = """\[\[([^\]]+)\]\]""".r
  val userSelector = """\[\[Utilisateur:([^\]]+)\]\]""".r

  val bot = new MediaWikiBot(config.getString("mediawiki"))

  val allPages = new AllPageTitles(bot).iterator().asScala.toList
  val allLiteralPages = allPages.filter(p => atLeastOneChar.findFirstIn(p).isDefined)

  val article = bot.getArticle("Bots")
  //println(article.getText)
  //println(hyperwordTokenizer(article))
  //println(usersTokenizer(article))
  //println(subtitleTokenizer(article))

  val licenses = Map(
    1 -> "http://creativecommons.org/licenses/by-nc-sa/2.0/",
    2 -> "http://creativecommons.org/licenses/by-nc/2.0/",
    3 -> "http://creativecommons.org/licenses/by-nc-nd/2.0/",
    4 -> "http://creativecommons.org/licenses/by/2.0/",
    5 -> "http://creativecommons.org/licenses/by-sa/2.0/",
    6 -> "http://creativecommons.org/licenses/by-nd/2.0/"
  )

  def tokenizer(content: String, regex: Regex): List[String] =
    regex.findAllMatchIn(content).map(_.group(1).trim).toList

  def hyperwordTokenizer(article: Article): List[String] =
    tokenizer(article.getText, hyperwordSelector)

  def usersTokenizer(article: Article): List[String] =
    tokenizer(article.getText, userSelector)

  def subtitleTokenizer(article: Article): List[(String, Int)] =
    article
      .getText
      .split('\n')
      .filter(line => line.startsWith("=") && line.endsWith("="))
      .map { line =>
        val level = line.trim.takeWhile(_ == '=').length
        val subtitle = line.trim.drop(level).dropRight(level).trim
        (subtitle, level)
      }
      .toList

  //bot.login(config.getString("login"), config.getString("password"))
  //article.save()
  //bot.getPerformedAction(new FileUpload(new SimpleFile(file), bot))

  allPages.foreach(p => {
    println(p)
    val i = Utils.parseRequest(p)
    if (i != null) {
      i.saveToFile()
    }
  })

}
