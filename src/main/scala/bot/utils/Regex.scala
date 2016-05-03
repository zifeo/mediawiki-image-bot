package bot.utils

object Regex {

  val atLeastOneChar = """[a-zA-Z]""".r
  val hyperwordSelector = """\[\[([^\]]+)\]\]""".r
  val userSelector = """\[\[Utilisateur:([^\]]+)\]\]""".r

}
