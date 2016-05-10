package bot.wiki

case class WikiImage(
                      filename: String,
                      author: Option[String],
                      url: String,
                      tags: List[String],
                      description: String,
                      license: String,
                      discarded: Boolean = false
                    ) {

  def wikitag: String =
    s"""[[File:$filename|thumb|200x200px|upright|$description]]\n"""

  def print(): Unit = {
    println("WikiImage " + filename + " : ")
    println("\tauthor     : " + author)
    println("\turl        : " + url)
    println("\ttags       : " + tags)
    println("\tdescription: " + description)
    println("\tlicence    : " + license)
    println("\tdiscarded  : " + discarded)
  }

}
