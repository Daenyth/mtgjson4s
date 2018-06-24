package mtgjson4s

import cats.effect.IO
import fs2.{Stream, StreamApp}

object Main extends StreamApp[IO] {
  override def stream(
      args: List[String],
      requestShutdown: IO[Unit]
  ): Stream[IO, StreamApp.ExitCode] =
    ???
}

// dump code here then move it out later
case class CardName(value: String) extends AnyVal
case class Set(name: String, code: String)
case class Muid(value: Int) extends AnyVal

trait Foo {
  type SetBuildResult = Unit // TODO

  def buildSet(setName: String): IO[SetBuildResult] = ???

  def downloadListOfCardsInSet(setName: String): Stream[IO, CardName] = ???
}
