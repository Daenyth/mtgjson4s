package mtgjson4s

import cats.Functor
import cats.effect.IO
import fs2.Stream
import org.http4s.{EntityDecoder, Uri}
import org.http4s.client.Client

import scala.concurrent.ExecutionContext
import cats.implicits._

object Download {
  private val SEARCH_URL = Uri.uri("http://gatherer.wizards.com/Pages/Search/Default.aspx")
  private val MAIN_URL = Uri.uri("http://gatherer.wizards.com/Pages/Card/Details.aspx")
  private val LEGAL_URL = Uri.uri("http://gatherer.wizards.com/Pages/Card/Printings.aspx")
  private val FOREIGN_URL = Uri.uri("http://gatherer.wizards.com/Pages/Card/Languages.aspx")
  private val TOKEN_URL =
    Uri.uri("https://raw.githubusercontent.com/cockatrice/Magic-Token/master/tokens.xml")

  def getSetCheckList(
      client: Client[IO],
      set: CardSet,
      maxParallelDownloads: Int
  )(
      implicit ec: ExecutionContext
  ): Stream[IO, Html[SetChecklist]] = {

    def getPage(pageNumber: Int): Stream[IO, Html[SetChecklist]] = {
      val url = SEARCH_URL
        .withQueryParam("output", "checklist")
        .withQueryParam("sort", "cn+")
        .withQueryParam("action", "advanced")
        .withQueryParam("special", "true")
        .withQueryParam("set", s"""["${set.name}"]""")
        .withQueryParam("page", pageNumber)

      Stream.eval(client.expect[Html[SetChecklist]](url))
    }

    getPage(0).flatMap { firstPage =>
      val getNonFirstPages: Stream[IO, Html[SetChecklist]] = Stream
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

  def getCardOracleDetails(
      client: Client[IO],
      muid: Muid
  ): IO[Html[CardOracleData]] =
    client.expect[Html[CardOracleData]](withCardParams(MAIN_URL, muid, printed = false))

  def getCardPrintedDetails(
      client: Client[IO],
      muid: Muid
  ): IO[Html[CardPrintedData]] =
    client.expect[Html[CardPrintedData]](withCardParams(MAIN_URL, muid, printed = true))

  def getCardLegalities(
      client: Client[IO],
      muid: Muid
  ): IO[Html[CardLegalities]] =
    client.expect[Html[CardLegalities]](withCardParams(LEGAL_URL, muid, printed = false))

  def getCardForeignData(
      client: Client[IO],
      muid: Muid
  ): IO[Html[CardForeignData]] =
    client.expect[Html[CardForeignData]](withCardParams(FOREIGN_URL, muid, printed = false))

  def getTokenXml(client: Client[IO]): IO[String] =
    client.expect[String](TOKEN_URL)

  def getCardDescription(
      client: Client[IO],
      muid: Muid
  ): IO[Either[Throwable, (CardDescription, Option[CardDescription])]] =
    (
      getCardOracleDetails(client, muid),
      getCardPrintedDetails(client, muid),
      getCardLegalities(client, muid),
      getCardForeignData(client, muid)
    ).parMapN(Parse.buildCardDesc)

  private def withCardParams(baseUri: Uri, muid: Muid, printed: Boolean): Uri =
    baseUri
      .withQueryParam("multiverseId", muid.value)
      .withQueryParam("printed", printed)
      .withQueryParam("page", 0)
}

case class Html[+A <: PageType](text: String) extends AnyVal

object PageType {
  implicit def entityDecoderForPageType[F[_]: Functor, A <: PageType](
      implicit decoder: EntityDecoder[F, String]
  ): EntityDecoder[F, Html[A]] =
    decoder.map(Html[A])
}
sealed trait PageType

/** Search - by set name, checklist format */
case class SetChecklist() extends PageType

/** Get single card by muid, oracle text */
case class CardOracleData() extends PageType

/** Get single card by muid, printed text */
case class CardPrintedData() extends PageType

case class CardLegalities() extends PageType

case class CardForeignData() extends PageType
