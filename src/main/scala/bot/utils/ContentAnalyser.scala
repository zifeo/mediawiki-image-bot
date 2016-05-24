package com.zifeo.mailsort.mining

import java.util.{List => JList, Properties}
import javax.mail.internet.InternetAddress

import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.{LanguageDetectorBuilder}
import com.optimaize.langdetect.profiles.LanguageProfileReader
import com.optimaize.langdetect.text.{CommonTextObjectFactories}
import com.zifeo.mailsort.mining.ContentAnalyser.{Result, POS, NER, shortText, largeText, langDetector}
import edu.stanford.nlp.ling.CoreAnnotations._
import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.util.CoreMap
import edu.stanford.nlp.util.logging.RedwoodConfiguration

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.Try

import scala.language.implicitConversions
/**
  * A content analysis using NLP Stanford package for Named Entity Relation (NER),
  * word lemmas (LEM) and Part of Speech (POS) tagging.
  *
  * @see see [The Stanford Natural Language Processing Group](http://nlp.stanford.edu/software/index.shtml) for more
  * @constructor create a new text analysis
  * @param content raw text
  */
class ContentAnalyser(private val content: String) {

  private val props = new Properties
  props.put("annotators", "tokenize, cleanxml, ssplit, parse, lemma, ner") // steps

  private val pipeline = new StanfordCoreNLP(props)
  private val document = new Annotation(content.take(500))
  pipeline.annotate(document)

  /** Organize results tupled with the initial word, the POS tag, the word lemma and NER tag. */
  val results: List[Result] = for {
    sentence <- document.get[JList[CoreMap]](classOf[SentencesAnnotation]).asScala.toList
    token <- sentence.get[JList[CoreLabel]](classOf[TokensAnnotation]).asScala
    word: String = token.get[String](classOf[TextAnnotation])
    pos: String = token.get[String](classOf[PartOfSpeechAnnotation])
    lem: String = token.get[String](classOf[LemmaAnnotation])
    ner: String = token.get[String](classOf[NamedEntityTagAnnotation])
  } yield (word, POS(pos), lem.toLowerCase, NER(ner))

  /** Filter only *meaningful* part from analysis (i.e. verbs, common nouns, adjectives and adverbs). */
  lazy val meaningful: List[String] =
    for {
      (_, pos, lem, _) <- results if POS.verb(pos) || POS.commonNoun(pos) || POS.adjective(pos) || POS.adverb(pos)
    } yield lem

  /** Filter person names by POS and NER. */
  lazy val persons: List[List[Result]] = {
    val byPOS = selectResults { case (_, pos, _, _) => POS.properNoun(pos) }(results)
    val byNER = selectResults { case (_, _, _, ner) => ner == NER.Person }(results)
    (byPOS ++ byNER).distinct
  }

  /** Filter mail adresses by looking for the '@' char. */
  lazy val mails: List[InternetAddress] =
    for {
      (v, _, _, _) <- results
      if v.contains('@')
      converted = Try{ new InternetAddress(v) }
      if converted.isSuccess
    } yield converted.get

  /** Detect the lang of the content. */
  lazy val lang: String = {

    val shortExtract = shortText.forText(content.take(100))
    val langShort = langDetector.detect(shortExtract)

    if (!langShort.isPresent) {

      val largeExtract = largeText.forText(content.take(500))
      val langLarge = langDetector.detect(largeExtract)

      if (!langLarge.isPresent) ""
      else langLarge.get

    } else langShort.get
  }


  /**
    * Select all results that satisfy the given function.
    *
    * @param f result selection function
    * @param results list of results
    * @return list of matched results list
    */
  def selectResults(f: Result => Boolean)(results: List[Result]): List[List[Result]] = {

    /** Tail recursive on acc. */
    @tailrec
    def rec(results: List[Result], acc: List[List[Result]]): List[List[Result]] =
      results match {
        case l @ (x :: _) if f(x) =>
          val (person, resultsLeft) = getNextResultsFor(f)(l, 3)
          rec(resultsLeft, person :: acc)
        case _ :: xs => rec(xs, acc)
        case Nil => acc
      }

    rec(results, Nil)
  }

  /**
    * Find all next results that satisfy the given function.
    * The result are separated by at most maxSeparation other results.
    * The search is stopped if maxSeparation results did not match or if the end of list is reached.
    *
    * @param f result selection function
    * @param results list of results
    * @param maxSeparation maximum space between two results satisfying the selection function
    * @return tuple of the matched results list and the left results list
    */
  def getNextResultsFor(f: Result => Boolean)(results: List[Result], maxSeparation: Int): (List[Result], List[Result]) = {

    /** Tail recursive on acc. */
    @tailrec
    def rec(results: List[Result], n: Int, acc: List[Result]): (List[Result], List[Result]) =
      results match {
        case x :: xs if n > 0 =>
          if (f(x))
            rec(xs, maxSeparation, x :: acc)
          else
            rec(xs, n - 1, acc)
        case _ => (acc, results)
      }

    rec(results, maxSeparation, Nil)
  }

}

/** Companion object for [[ContentAnalyser]]. */
object ContentAnalyser {

  type Result = (String, POS.V, String, NER.V)


  /** A type of enumeration with defined helper methods for Stanford tag rules. */
  trait StanfordNamings extends Enumeration {
    type V = Value
    val Misc: V = "Misc"

    /**
      * Retrieve corresponding value with given name or default value.
      *
      * @param name value name
      * @return corresponding value or default value
      */
    def apply(name: String): Value =
      try withName(name)
      catch {
        case _: NoSuchElementException => Misc
      }

    /** Implicitly convert string to value. */
    implicit def stringToValue(s: String): V = Value(s)
  }

  /** Named Entity Relation enumeration following Stanford NER tags. */
  object NER extends StanfordNamings {
    val Time: V = 			"Time"
    val Location: V = 		"Location"
    val Organization: V = 	"Organization"
    val Person: V = 		"Person"
    val Money: V = 			"Money"
    val Percent: V = 		"Percent"
    val Date: V = 			"Date"
  }

  /** Part of Speech enumeration following Stanford POS tags. */
  object POS extends StanfordNamings {
    val CC: V = 	"CC" // conjunction, coordinating (& and both minus therefore vs.)
    val CD: V = 	"CD" // numeral, cardinal (mid-1890 nine-thirty 0.5 '79)
    val DT: V = 	"DT" // determiner (all an another any this)
    val EX: V = 	"EX" // existential there
    val FW: V = 	"FW" // foreign word
    val IN: V = 	"IN" // preposition or conjunction, subordinating (among near if whether out)
    val JJ: V = 	"JJ" // adjective or numeral, ordinal (third ill-mannered pre-war)
    val JJR: V = 	"JJR" // adjective, comparative (braver colder)
    val JJS: V = 	"JJS" // adjective, superlative (cheapest cutest)
    val LS: V = 	"LS" // list item marker (A. First One *)
    val MD: V = 	"MD" // modal auxiliary (can cannot could couldn't dare)
    val NN: V = 	"NN" // noun, common, singular or mass (cabbage shed)
    val NNS: V = 	"NNS" // noun, common, plural (scotches clubs)
    val NNP: V = 	"NNP" // noun, proper, singular (Ranzer Conchita)
    val NNPS: V = 	"NNPS" // noun, proper, plural (Amharas Animals)
    val PDT: V = 	"PDT" // pre-determiner (many quite such sure this)
    val V: V = 	 	"POS" // genitive marker (' 's)
    val PRP: V = 	"PRP" // pronoun, personal (him himself hisself it)
    val PRP$: V = 	"PRP$" // pronoun, possessive (her his mine my our)
    val RB: V = 	"RB" // adverb (adventurously predominately)
    val RBR: V = 	"RBR" // adverb, comparative (graver greater)
    val RBS: V = 	"RBS" // adverb, superlative (furthest hardest)
    val RP: V = 	"RP" // particle (about across along apart i.e. in with)
    val SYM: V = 	"SYM" // symbol (% & ' '' ''. ) ). * + ,. < = > @ A[fj] U.S U.S.S.R * ** ***)
    val TO: V = 	"TO" // "to" as preposition or infinitive marker
    val UH: V = 	"UH" // interjection (Goodbye Goody Gosh Wow Jeepers Jee-sus)
    val VB: V = 	"VB" // verb, base form (ask assemble assess assign)
    val VBD: V = 	"VBD" // verb, past tense (dipped pleaded figgered)
    val VBG: V = 	"VBG" // verb, present participle or gerund (telegraphing stirring focusing)
    val VBN: V = 	"VBN" // verb, past participle (languished panelized used)
    val VBP: V = 	"VBP" // verb, present tense, not 3rd person singular (sue twist spill cure)
    val VBZ: V = 	"VBZ" // verb, present tense, 3rd person singular (bases reconstructs marks)
    val WDT: V = 	"WDT" // WH-determiner (that what whatever which whichever)
    val WP: V = 	"WP" // WH-pronoun (that what whatever whatsoever)
    val WP$: V = 	"WP$" // WH-pronoun, possessive (whose)
    val WRB: V = 	"WRB" // Wh-adverb (how however whence whenever where whereby)

    /** Check whether it is a verb. */
    def verb(pos: V): Boolean =
      pos == VB || pos == VBD || pos == VBG || pos == VBN || pos == VBP || pos == VBZ

    /** Check whether it is a common noun. */
    def commonNoun(pos: V): Boolean =
      pos == NN || pos == NNS

    /** Check whether it is a proper noun. */
    def properNoun(pos: V): Boolean =
      pos == NNP || pos == NNPS

    /** Check whether it is an adjective. */
    def adjective(pos: V): Boolean =
      pos == JJ || pos == JJR || pos == JJS

    /** Check whether it is an adverb. */
    def adverb(pos: V): Boolean =
      pos == RB || pos == RBR || pos == RBS

  }

  // language dectector objects
  private val langProfiles = new LanguageProfileReader().readAll
  val langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard).withProfiles(langProfiles).build
  val shortText = CommonTextObjectFactories.forDetectingShortCleanText
  val largeText = CommonTextObjectFactories.forDetectingOnLargeText

}