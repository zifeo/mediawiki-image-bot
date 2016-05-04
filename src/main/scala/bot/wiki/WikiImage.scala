package bot.wiki

import spray.json._
import bot._

case class WikiImage(
                      url: String,
                      description: String,
                      license: String,
                      filename: String,
                      discarded: Boolean = false
                    ) {

}
