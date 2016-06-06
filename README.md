## Mediawiki imagebot

This is a bot project part of the EPFL's Digital Humanities 2016 course. It aims to illustrate each [Mediawiki](https://www.mediawiki.org/wiki/MediaWiki/fr)-like page using both [Google Images API](https://developers.google.com/custom-search/) and [Flickr API](https://www.flickr.com/services/api/) as [Creative Commons](http://creativecommons.org) image sources.

### Setup

All the configuration is located in `src/main/resources/application.conf`, change the fields accordingly. The files `flickr.ks` and `google.keys` should be created in the root folders and contains respective api keys. Note that you can add more than one Google key.

```bash
cat ./flickr.ks
key
secret
```

```bash
cat ./google.keys
key1
key2
```

### Scenarios

A scenario is specific task using the predefined imagebot functions.

```scala
// single page example
object BotASable extends BotApp {
  bot.signIn()
  val Some(page) = bot.load("BotASable")
  GoogleSearch(page.title) march {
    case (image, file) :: _ => bot.add(page, image, file)
    case _ => // no result
  }
}
```

### License

Project is available under [CC-BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/) and data belong their owners under appropriate licensing.
