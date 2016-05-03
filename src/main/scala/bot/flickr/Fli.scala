package bot.flickr

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

import bot.utils.Regex
import com.flickr4java.flickr.photos.{Photo, SearchParameters, Size}
import com.flickr4java.flickr.{Flickr, REST}
import net.sourceforge.jwbf.core.contentRep.Article
import net.sourceforge.jwbf.mediawiki.actions.queries.AllPageTitles
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot

import scala.collection.JavaConverters._

import bot._

object Fli {

  val bot = new MediaWikiBot(config.getString("mediawiki"))

  val allPages = new AllPageTitles(bot).iterator().asScala.toList
  val allLiteralPages = allPages.filter(p => Regex.atLeastOneChar.findFirstIn(p).isDefined)

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

  val flickr = new Flickr(config.getString("flickr.key"), config.getString("flickr.secret"), new REST)

  val res = searchPhotos("EPFL")

  for (pid <- res.take(1)) {

    val photo = flickr.getPhotosInterface.getInfo(pid.getId, null)
    println(photo.getTitle)
    println(Option(photo.getDescription))
    println(photo.getTags.asScala.toList.map(_.getValue))
    println(licenses(photo.getLicense.toInt))
    println(photo.getOwner.getUsername)
    println(Option(photo.getOwner.getRealName).filter(_.nonEmpty))
    println(photo.getMediumUrl)

    val file = fileFromPhoto(photo)

    println("*****")

  }

  def searchPhotos(terms: String): List[Photo] = {
    val params = new SearchParameters
    params.setText(terms)
    params.setLicense(licenses.keys.mkString(","))
    params.setMedia("photos")
    params.setSort(SearchParameters.INTERESTINGNESS_DESC)
    params.setSafeSearch(Flickr.SAFETYLEVEL_SAFE)
    flickr.getPhotosInterface.search(params, 5, 1).asScala.toList
  }

  def fileFromPhoto(photo: Photo): File = {
    val file = File.createTempFile("mediawiki-image-bot-", ".jpg")
    file.deleteOnExit()
    val stream = flickr.getPhotosInterface.getImageAsStream(photo, Size.MEDIUM)
    Files.copy(stream, file.toPath, StandardCopyOption.REPLACE_EXISTING)
    stream.close()
    file
  }

  //bot.login(config.getString("login"), config.getString("password"))
  //article.save()
  //bot.getPerformedAction(new FileUpload(new SimpleFile(file), bot))

}