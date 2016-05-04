package bot.providers

import java.io.File
import java.net.URLEncoder

import bot._
import bot.wiki.WikiImage
import com.flickr4java.flickr.photos.{SearchParameters, Size}
import com.flickr4java.flickr.{Flickr, REST}

import scala.collection.JavaConverters._

object FlickrSearch {

  private val searchCount = 5
  private val flickr = new Flickr(config.getString("flickr.key"), config.getString("flickr.secret"), new REST)

  // http://creativecommons.org/licenses/x
  val licenses = Map(
    1 -> "by-nc-sa/2.0",
    2 -> "by-nc/2.0",
    3 -> "by-nc-nd/2.0",
    4 -> "by/2.0",
    5 -> "by-sa/2.0",
    6 -> "by-nd/2.0"
  )

  def apply(terms: String): List[(WikiImage, File)] = {
    log.info("Flickr searching for {}", terms)

    val params = new SearchParameters
    params.setText(terms)
    params.setLicense(licenses.keys.mkString(","))
    params.setMedia("photos")
    params.setSort(SearchParameters.INTERESTINGNESS_DESC)
    params.setSafeSearch(Flickr.SAFETYLEVEL_SAFE)

    flickr
      .getPhotosInterface
      .search(params, searchCount, 1)
      .asScala
      .toList
      .map { res =>
        val photo = flickr.getPhotosInterface.getInfo(res.getId, null)

        val filename = photo.getTitle
        val description = Option(photo.getDescription).map(_.take(50).trim).getOrElse(filename)
        val author = Some(
          if (Option(photo.getOwner.getRealName).getOrElse("").nonEmpty) photo.getOwner.getRealName
          else photo.getOwner.getUsername
        )
        val license = licenses(photo.getLicense.toInt)
        val tags = photo.getTags.asScala.toList.map(_.getValue)
        val file = tempFileFromStream(flickr.getPhotosInterface.getImageAsStream(photo, Size.MEDIUM))
        val ext = file.getPath.reverse.takeWhile(_ != '.').reverse

        log.debug("Found: {}", filename)
        WikiImage(URLEncoder.encode(filename, "UTF-8") + s".$ext", author, photo.getUrl, tags, description, license) -> file
      }
  }

}
