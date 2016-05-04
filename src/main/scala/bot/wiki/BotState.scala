package bot.wiki

case class BotState (
                      botPage: String,
                      revisionId: String,
                      pages: Set[WikiPage]
                    )
