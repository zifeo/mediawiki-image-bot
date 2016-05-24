package bot.providers

import java.io.File

import akka.stream.javadsl.Source
import bot._
import bot.wiki.WikiImage
import com.flickr4java.flickr.photos.{SearchParameters, Size}
import com.flickr4java.flickr.{Flickr, REST}
import scala.io.Source

import scala.collection.JavaConverters._

object FlickrSearch {

  private val searchCount = 5
  private val keySecret = loadFlickrKeyAndSecret()
  private val flickr = new Flickr(keySecret._1, keySecret._2, new REST)

  def loadFlickrKeyAndSecret() : (String, String) =  {
    val filename = config.getString("flickrKeyAndSecretFile")
    // Quick and dirty, we expect 2 lines, the first one being the key and the second one the secret
    val lines = scala.io.Source.fromFile(filename).getLines()
    val key = lines.next
    val secret = lines.next
    (key, secret)
  }

  // http://creativecommons.org/licenses/x
  val licenses = Map(
    1 -> "by-nc-sa/2.0",
    2 -> "by-nc/2.0",
    3 -> "by-nc-nd/2.0",
    4 -> "by/2.0",
    5 -> "by-sa/2.0",
    6 -> "by-nd/2.0"
  )

  def apply(terms: String): Stream[(WikiImage, File)] = {
    log.info("Flickr searching for {}", terms)

    val params = new SearchParameters
    params.setText(terms)
    params.setLicense(licenses.keys.mkString(","))
    params.setMedia("photos")
    params.setSort(SearchParameters.RELEVANCE)
    params.setSafeSearch(Flickr.SAFETYLEVEL_SAFE)

    flickr
      .getPhotosInterface
      .search(params, searchCount, 1)
      .asScala
      .toStream
      .map { res =>
        val photo = flickr.getPhotosInterface.getInfo(res.getId, null)

        val name = cleanName(photo.getTitle).trim
        val description = Option(photo.getDescription).map(_.take(200).trim).getOrElse(name)
        val author = Some(
          if (Option(photo.getOwner.getRealName).getOrElse("").nonEmpty) photo.getOwner.getRealName
          else photo.getOwner.getUsername
        )
        val license = licenses(photo.getLicense.toInt)
        val tags = photo.getTags.asScala.toList.map(_.getValue)
        val file = tempFileFromStream(flickr.getPhotosInterface.getImageAsStream(photo, Size.MEDIUM))

        log.debug("Found: {}", name)
        WikiImage(s"$name.png", author, photo.getUrl, tags, description, license) -> file
      }
  }

}
