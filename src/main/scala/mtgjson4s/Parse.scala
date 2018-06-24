package mtgjson4s

import cats.implicits._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.jsoup.Jsoup

object Parse {
  def parseString(html: Html[_]): Either[Throwable, JsoupDocument] =
    Either.catchNonFatal(JsoupDocument(Jsoup.parse(html.text)))

  def getLastPageNumber(page: Html[SetChecklist]): Either[Throwable, Int] =
    for {
      doc <- Parse.parseString(page)
      pagingControl <- (doc >> elementList("div .pagingcontrols")).lastOption
        .toRight(new Exception("Page has no .pagingcontrols div"))
      links = pagingControl >> elementList("a")
      pageNumbers = links
        .map(e => Either.catchNonFatal(e.text.toInt))
        .collect { case Right(i) => i }
      lastPageNumber <- pageNumbers.lastOption.toRight(new Exception("no page numbers"))
    } yield lastPageNumber - 1 // "- 1" because the url param is 0+ and text is 1+

  def getMuidsFromPage(
      html: Html[SetChecklist]
  ): Either[Throwable, List[ChecklistCardInfo]] =
    parseString(html).map { doc =>
      val cards = doc >> elementList(".cardItem")
      cards.map { card =>
        val name = card >> text(".name")
        val link = (card >> element(".nameLink")).attr("href")
        val muid = link.split("=").last.toInt
        ChecklistCardInfo(name, Muid(muid))
      }
    }

  def buildCardDesc(
      oracleData: Html[CardOracleData],
      printedData: Html[CardPrintedData],
      legalities: Html[CardLegalities],
      foreignData: Html[CardForeignData]
  ): Either[Throwable, CardDescription] = ???

}

case class ChecklistCardInfo(name: String, muid: Muid)
