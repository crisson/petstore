name := "petstore-tapir"

description := "Testing tapir with the Petstore model"

scalaVersion := "2.12.8"

organization := "me.crisson.petstore"

mainClass in (Compile, run) := Some("me.crisson.petstore.Main")

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    `backend-cloudflare`,
    api,
    swagger
  )

lazy val `backend-cloudflare` = project
  .settings(
    name := "backend-cloudflare",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.fs2,
      dependencies.fs2Io,
      dependencies.cats,
      dependencies.catsEffect
    )
  )
  .dependsOn(api)

lazy val api = project
  .settings(
    name := "api",
    settings,
    libraryDependencies ++= commonDependencies ++ circeDependencies ++ Seq(
      dependencies.tapir,
      dependencies.tapirCirce,
      dependencies.tapirHttp4s,
      dependencies.cats,
      dependencies.catsEffect
    )
  )
  .disablePlugins(AssemblyPlugin)

lazy val swagger = project
  .settings(
    name := "swagger",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .dependsOn(api)

lazy val dependencies =
  new {
    val logbackV        = "1.2.3"
    val logstashV       = "4.11"
    val scalaLoggingV   = "3.7.2"
    val slf4jV          = "1.7.25"
    val typesafeConfigV = "1.3.1"
    val pureconfigV     = "0.8.0"
    val monocleV        = "1.4.0"
    val scalatestV      = "3.0.4"
    val scalacheckV     = "1.13.5"
    val circeVersion    = "0.11.1"
    val tapirVersion    = "0.7.10"
    val catsVersion     = "2.0.0-M1"
    val http4sVersion   = "0.20.1"
    val fs2Version      = "1.0.4"

    val fs2   = "co.fs2" %% "fs2-core" % fs2Version
    val fs2Io = "co.fs2" %% "fs2-io"   % fs2Version

    val http4sServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
    val http4sCirce  = "org.http4s" %% "http4s-circe"        % http4sVersion
    val http4sDsl    = "org.http4s" %% "http4s-dsl"          % http4sVersion

    val cats       = "org.typelevel" %% "cats-core"   % catsVersion
    val catsEffect = "org.typelevel" %% "cats-effect" % "1.3.0"

    val circe        = "io.circe" %% "circe-core"    % circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeParser  = "io.circe" %% "circe-parser"  % circeVersion

    val tapir       = "com.softwaremill.tapir" %% "tapir-core"          % tapirVersion
    val tapirCirce  = "com.softwaremill.tapir" %% "tapir-json-circe"    % tapirVersion
    val tapirHttp4s = "com.softwaremill.tapir" %% "tapir-http4s-server" % tapirVersion

    val logback        = "ch.qos.logback"             % "logback-classic"          % logbackV
    val logstash       = "net.logstash.logback"       % "logstash-logback-encoder" % logstashV
    val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"           % scalaLoggingV
    val slf4j          = "org.slf4j"                  % "jcl-over-slf4j"           % slf4jV
    val typesafeConfig = "com.typesafe"               % "config"                   % typesafeConfigV
    val monocleCore    = "com.github.julien-truffaut" %% "monocle-core"            % monocleV
    val monocleMacro   = "com.github.julien-truffaut" %% "monocle-macro"           % monocleV
    val pureconfig     = "com.github.pureconfig"      %% "pureconfig"              % pureconfigV
    val scalatest      = "org.scalatest"              %% "scalatest"               % scalatestV
    val scalacheck     = "org.scalacheck"             %% "scalacheck"              % scalacheckV
  }

lazy val commonDependencies = Seq(
  dependencies.logback,
  dependencies.logstash,
  dependencies.scalaLogging,
  dependencies.slf4j,
  dependencies.pureconfig,
  dependencies.typesafeConfig,
  dependencies.scalatest  % "test",
  dependencies.scalacheck % "test"
)

lazy val circeDependencies = Seq(
  dependencies.circe,
  dependencies.circeGeneric,
  dependencies.circeParser
)

lazy val tapirDependencies = Seq(
  dependencies.tapir,
  dependencies.tapirCirce,
  dependencies.tapirHttp4s
)

lazy val settings =
commonSettings ++
wartremoverSettings ++
scalafmtSettings

lazy val scalafmtSettings = Seq(
  scalafmtOnCompile := true
)

lazy val wartremoverSettings = Seq(
  // wartremoverWarnings in (Compile, compile) ++= Warts.allBut(Wart.Throw)
)

lazy val compilerOptions = Seq(
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

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)
