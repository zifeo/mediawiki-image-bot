package bot.utils

import net.sourceforge.jwbf.core.contentRep.Article

import scala.util.matching.Regex

object Tokenizer {

  private val hyperwordSelector = """\[\[([^\]]+)\]\]""".r
  private val userSelector = """\[\[Utilisateur:([^\]]+)\]\]""".r

  private def tokenizer(content: String, regex: Regex): List[String] =
    regex.findAllMatchIn(content).map(_.group(1).trim).toList

  def hyperword(article: Article): List[String] =
    tokenizer(article.getText, hyperwordSelector)

  def users(article: Article): List[String] =
    tokenizer(article.getText, userSelector)

  def subtitle(article: Article): List[(String, Int)] =
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
