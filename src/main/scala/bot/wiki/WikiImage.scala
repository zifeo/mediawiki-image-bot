package bot.wiki

case class WikiImage(
                      filename: String,
                      author: Option[String],
                      url: String,
                      tags: List[String],
                      description: String,
                      license: String,
                      discarded: Boolean = false
                    )
