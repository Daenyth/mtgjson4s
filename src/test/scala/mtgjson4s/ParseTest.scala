package mtgjson4s

import org.scalatest.FlatSpec

class ParseTest extends FlatSpec with BaseTest {
  behavior of "Parsing"

  private val search7e: Html[SetChecklist] =
    getHtml[SetChecklist]("search-7e.html").unsafeRunSync()

  it should "parse page number" in {
    val result = Parse.getLastPageNumber(search7e)
    result.right.value shouldBe 3
  }

  it should "get muids from page" in {
    val result = Parse.getMuidsFromPage(search7e).right.value
    result.length shouldBe 100
    result.head shouldBe ChecklistCardInfo("Angelic Page", Muid(25546))
  }
}
