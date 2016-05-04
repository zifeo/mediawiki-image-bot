package bot.wiki

object PageType extends Enumeration {

  type PageType = Value

  val BLACKLISTED, UNCLASSIFIED, LITERAL, DATE, LOCATION, USER = Value

}
