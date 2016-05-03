package bot.wiki

object PageType extends Enumeration {
  type PageType = Value

  val NONE = Value
  val ARTICLE = Value
  val DATE = Value
  val LOCATION = Value
  val USER = Value

}
