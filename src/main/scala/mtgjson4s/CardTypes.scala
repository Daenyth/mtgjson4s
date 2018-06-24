package mtgjson4s

import org.apache.commons.codec.digest.DigestUtils

// TODO fix Options, nest things like power/tough under "CreatureFields" type etc
case class CardDescription(
    artist: List[String],
    cardHash: Option[String],
    convertedManaCost: Option[Either[Int, Float]],
    colorIdentity: List[Color],
    colors: List[Color],
    flavor: Option[String],
    foreignData: List[ForeignNamesDescription],
    layout: Option[String],
    legalities: List[Map[String, String]],
    loyalty: Option[String],
    manaCost: Option[String],
    muid: Option[Muid],
    name: String,
    names: List[String],
    number: Option[String],
    originalText: Option[String],
    originalType: Option[String],
    power: Option[String],
    printings: List[String],
    rarity: Option[String],
    rulings: List[Map[String, String]],
    subtypes: List[String],
    supertypes: List[String],
    text: Option[String],
    toughness: Option[String],
    cardType: Option[String],
    types: List[String],
    hand: Option[String],
    life: Option[String],
    watermark: Option[String],
    reserved: Option[Boolean],
    variations: List[Int],
) {

  /** @return Some when `muid` is Some */
  def uniqueHash(set: CardSet): Option[String] = muid.map { muid =>
    val d = DigestUtils.getSha256Digest
    d.update(set.name.getBytes("UTF-8"))
    d.update(muid.value.toString.getBytes("UTF-8"))
    d.update(name.getBytes("UTF-8"))
    DigestUtils.sha256Hex(d.digest())
  }
}

case class ForeignNamesDescription(
    language: Option[String],
    muid: Option[Muid],
    name: Option[String],
    cardType: Option[String],
    text: Option[String],
    flavor: Option[String],
)

case class Color(value: String) extends AnyVal
case class Muid(value: Int) extends AnyVal
case class CardSet(name: String, code: String)
