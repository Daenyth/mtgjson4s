package mtgjson4s

import cats.effect.IO
import fs2.Stream
import org.http4s.Uri
import org.http4s.client.Client

import scala.concurrent.ExecutionContext

object Download {
  val SEARCH_URL = Uri.uri("http://gatherer.wizards.com/Pages/Search/Default.aspx")
  val MAIN_URL = Uri.uri("http://gatherer.wizards.com/Pages/Card/Details.aspx")
  val LEGAL_URL = Uri.uri("http://gatherer.wizards.com/Pages/Card/Printings.aspx")
  val FOREIGN_URL = Uri.uri("http://gatherer.wizards.com/Pages/Card/Languages.aspx")
  val TOKEN_URL =
    Uri.uri("https://raw.githubusercontent.com/cockatrice/Magic-Token/master/tokens.xml")

  def getSetCheckList(
      client: Client[IO],
      set: Set,
      maxParallelDownloads: Int
  )(
      implicit ec: ExecutionContext
  ): Stream[IO, Html[CardSearchPage.type]] = {

    def getPage(pageNumber: Int): Stream[IO, Html[CardSearchPage.type]] = {
      val url = SEARCH_URL
        .withQueryParam("output", "checklist")
        .withQueryParam("sort", "cn+")
        .withQueryParam("action", "advanced")
        .withQueryParam("special", "true")
        .withQueryParam("set", s"""["${set.name}"]""")
        .withQueryParam("page", pageNumber)

      Stream.eval(client.expect[String](url).map(Html[CardSearchPage.type] _))
    }

    getPage(0).flatMap { firstPage =>
      val getNonFirstPages: Stream[IO, Html[CardSearchPage.type]] = Stream
        .emit(Parse.getLastPageNumber(firstPage))
        .covary[IO]
        .rethrow
        .flatMap { lastPageNumber =>
          Stream
            .range(1, lastPageNumber + 1)
            .covary[IO]
            .map(getPage)
        }
        .join(maxParallelDownloads)
      Stream.emit(firstPage) ++ getNonFirstPages
    }
  }

}

case class Html[+A <: PageType](text: String) extends AnyVal
sealed trait PageType
case object CardSearchPage extends PageType
