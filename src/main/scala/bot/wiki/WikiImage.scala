package bot.wiki

case class WikiImage(
                      url: String,
                      description: String,
                      license: String,
                      filename: String,
                      discarded: Boolean = false
                    ) {

}
