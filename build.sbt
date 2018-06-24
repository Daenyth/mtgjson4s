name := "mtgjson4s"

lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.12.6",
)

lazy val mtgjson4s = (project in file("."))
  .settings(commonSettings, name := "mtgjson4s")

// Remove these options in 'sbt console' because they're not nice for interactive usage
scalacOptions in (mtgjson4s, Compile, console) ~= (_.filterNot(Set("-Xfatal-warnings", "-Ywarn-unused-import").contains))

resolvers += Resolver.sonatypeRepo("releases")

val fs2Version = "0.10.5"
val dependencies = Seq(
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.typelevel" %% "cats-effect" % "0.10.1",
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version % "test",
  "net.ruippeixotog" %% "scala-scraper" % "2.1.0"
)
val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "com.ironcorelabs" %% "cats-scalatest" % "2.2.0" % "test"
)

libraryDependencies in mtgjson4s ++= dependencies
libraryDependencies in mtgjson4s ++= testDependencies

val http4sVersion = "0.18.4"
val circeVersion = "0.9.3"
libraryDependencies in mtgjson4s ++= Seq(
  "io.circe" %% "circe-core"    % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser"  % circeVersion,
  "io.circe" %% "circe-optics"  % circeVersion % "test",

  "org.http4s" %% "http4s-circe"        % http4sVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-client"       % http4sVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.1"
) ++ testDependencies


