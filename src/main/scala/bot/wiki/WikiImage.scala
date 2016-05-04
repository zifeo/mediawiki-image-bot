package bot.wiki

case class WikiImage(
                      filename: String,
                      author: Option[String],
                      url: String,
                      tags: List[String],
                      description: Option[String],
                      license: String,
                      discarded: Boolean = false
                    )
