package mtgjson4s

import java.io.File

import cats.effect.IO
import org.scalatest.{EitherValues, Matchers, OptionValues}

trait BaseTest extends OptionValues with EitherValues with Matchers {
  def getFile(name: String) =
    fs2.io.file
      .readAll[IO](
        new File(getClass.getResource(name).getFile).toPath,
        chunkSize = 4096
      )
      .through(fs2.text.utf8Decode)
      .compile
      .toVector
      .map(_.mkString(""))

  def getHtml[A <: PageType](name: String): IO[Html[A]] = getFile(name).map(Html[A])
}
