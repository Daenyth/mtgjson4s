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
  ): Either[Throwable, (CardDescription, Option[CardDescription])] = {
    buildOracleData(oracleData)
    println(printedData)
    println(legalities)
    println(foreignData)

    //    name_row = soup.find(id=parse_div.format('nameRow'))
    //    name_row = name_row.find_all('div')[-1]
    //    card_name = str(name_row.get_text(strip=True))
    ???
  }

  private[mtgjson4s] def buildOracleData(oracleData: Html[CardOracleData]) =
    parseString(oracleData).map { oracleDoc =>
      val cardCount = (oracleDoc >> elementList(".cardDetails")).length

      val clientId = "^ClientIDs\\.(\\S+) = '(.*)';".r
      val clientIds = (oracleDoc >> elementList("script"))
        .filter(_.innerHtml.contains("ClientIDs"))
        .flatMap(_.innerHtml.split("\r\n"))
        .collect { case clientId(key, value) => (key, value) }
        .toMap
      val nameDivClass = clientIds("nameRow")
      val (layout, secondCardNameDivClass) = cardCount match {
        case 1 => ("normal", None)
        case 2 =>
          ("double", Some(nameDivClass.replace("04", "05").replace("03", "04").replace("02", "03")))
      }

      println((layout, secondCardNameDivClass))
    }
}

case class ChecklistCardInfo(name: String, muid: Muid)
