name := "petstore-tapir"

description := "Testing tapir with the Petstore model"

scalaVersion := "2.12.8"

organization := "me.crisson.petstore"

mainClass in (Compile, run) := Some("me.crisson.petstore.Main")

val circeVersion  = "0.11.1"
val tapirVersion  = "0.7.10"
val catsVersion   = "2.0.0-M1"
val http4sVersion = "0.20.1"
val fs2Version    = "1.0.4"

libraryDependencies := Seq(
  "org.typelevel" %% "cats-effect" % "1.3.0"
) ++ Seq(
  "co.fs2" %% "fs2-core",
  "co.fs2" %% "fs2-io"
).map(_ % fs2Version) ++ Seq(
  "com.softwaremill.tapir" %% "tapir-core",
  "com.softwaremill.tapir" %% "tapir-json-circe",
  "com.softwaremill.tapir" %% "tapir-http4s-server"
).map(_ % tapirVersion) ++ Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion) ++ Seq(
  "org.typelevel" %% "cats-core"
).map(_ % catsVersion) ++ Seq(
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-dsl"
).map(_ % http4sVersion)

scalacOptions := Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)
