package bot

import net.sourceforge.jwbf.core.contentRep.Article

import scala.util.matching.Regex

object Tokenizer {

  private val hyperwordSelector = """\[\[([^\]]+)\]\]""".r
  private val userSelector = """\[\[Utilisateur:([^\]]+)\]\]""".r

  def tokenizer(content: String, regex: Regex): List[String] =
    regex.findAllMatchIn(content).map(_.group(1).trim).toList

  def hyperwordTokenizer(article: Article): List[String] =
    tokenizer(article.getText, hyperwordSelector)

  def usersTokenizer(article: Article): List[String] =
    tokenizer(article.getText, userSelector)

  def subtitleTokenizer(article: Article): List[(String, Int)] =
    article
      .getText
      .split('\n')
      .filter(line => line.startsWith("=") && line.endsWith("="))
      .map { line =>
        val level = line.trim.takeWhile(_ == '=').length
        val subtitle = line.trim.drop(level).dropRight(level).trim
        (subtitle, level)
      }
      .toList

}
