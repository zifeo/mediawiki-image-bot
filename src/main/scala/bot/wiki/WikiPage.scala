package bot.wiki

import bot.wiki.PageType.PageType

case class WikiPage(
                     title: String,
                     revisionId: String,
                     pageType: PageType,
                     images: List[WikiImage]
                   )
