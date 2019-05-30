resolvers += Resolver.typesafeRepo("eleases") 
resolvers += Resolver.typesafeIvyRepo("releases") 

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")