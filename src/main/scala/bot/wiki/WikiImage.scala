package bot.wiki

case class WikiImage(
                      filename: String,
                      author: String,
                      url: String,
                      tags: List[String],
                      description: String,
                      license: String,
                      discarded: Boolean = false
                    ) {

}
