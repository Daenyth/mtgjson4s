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
