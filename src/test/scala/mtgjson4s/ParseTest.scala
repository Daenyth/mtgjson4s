package mtgjson4s

import org.scalatest.FlatSpec

class ParseTest extends FlatSpec with BaseTest {
  behavior of "Parsing"

  private lazy val search7e: Html[SetChecklist] =
    getHtml[SetChecklist]("search-7e.html").unsafeRunSync()

  private lazy val cardOracle: Html[CardOracleData] =
    getHtml[CardOracleData]("card-25546.html").unsafeRunSync()

  lazy val splitCardOracle: Html[CardOracleData] =
    getHtml[CardOracleData]("card-292753.html").unsafeRunSync()

  it should "parse page number" in {
    val result = Parse.getLastPageNumber(search7e)
    result.right.value shouldBe 3
  }

  it should "get muids from page" in {
    val result = Parse.getMuidsFromPage(search7e).right.value
    result.length shouldBe 100
    result.head shouldBe ChecklistCardInfo("Angelic Page", Muid(25546))
  }

  it should "parse layout for regular cards" in {
    val x = Parse.buildOracleData(cardOracle)
    println(x)
    ???
  }
}
