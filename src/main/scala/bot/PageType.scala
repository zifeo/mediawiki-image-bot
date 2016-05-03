package bot

object PageType extends Enumeration {
  type PageType = Value

  val NONE = Value("NONE")
  val ARTICLE = Value("ARTICLE")
  val DATE = Value("DATE")
  val LOCATION = Value("LOCATION")
  val USER = Value("UTILISATEUR")

}
