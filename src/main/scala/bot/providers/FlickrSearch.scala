package bot.providers

import java.io.File

import bot._
import bot.wiki.WikiImage
import com.flickr4java.flickr.photos.{SearchParameters, Size}
import com.flickr4java.flickr.{Flickr, REST}

import scala.collection.JavaConverters._

object FlickrSearch {

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
    val params = new SearchParameters
    params.setText(terms)
    params.setLicense(licenses.keys.mkString(","))
    params.setMedia("photos")
    params.setSort(SearchParameters.INTERESTINGNESS_DESC)
    params.setSafeSearch(Flickr.SAFETYLEVEL_SAFE)

    flickr
      .getPhotosInterface
      .search(params, 5, 1)
      .asScala
      .toList
      .map { res =>
        val photo = flickr.getPhotosInterface.getInfo(res.getId, null)

        val title = photo.getTitle
        val description = Option(photo.getDescription).map(d => s" · $d").getOrElse("")
        val author =
          if (Option(photo.getOwner.getRealName).getOrElse("").nonEmpty) photo.getOwner.getRealName
          else photo.getOwner.getUsername

        val license = licenses(photo.getLicense.toInt)
        val tags = photo.getTags.asScala.toList.map(_.getValue)
        val file = tempFileFromStream(flickr.getPhotosInterface.getImageAsStream(photo, Size.MEDIUM))

        WikiImage("", author, photo.getUrl, tags, s"$title$description", license) -> file
      }
  }

}
