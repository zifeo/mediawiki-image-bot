package bot

import com.zifeo.mailsort.mining.ContentAnalyser

object NLPtest extends App {
  val cntAnalyser = new ContentAnalyser("Hello my name is John, and I love eating chicken");
  println(cntAnalyser.lang);
  println(cntAnalyser.meaningful);
  println(cntAnalyser.results)
}
