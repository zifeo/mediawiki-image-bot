package bot

class WikiImage(
                 val snippet: String,
                 val url: String,
                 val thumbnail: String,
                 var filename: String
               ) {

  override def toString: String = {
    ("\t\t\t\t{\n" +
      "\t\t\t\t\"snippet\" : \"%s\",\n" +
      "\t\t\t\t\"url\" : \"%s\",\n" +
      "\t\t\t\t\"thumbnail\" : \"%s\",\n" +
      "\t\t\t\t\"filename\" : \"%s\"\n" +
      "\t\t\t\t}").
      format(snippet, url, thumbnail, filename)
  }

}
