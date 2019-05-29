resolvers += Resolver.typesafeRepo("eleases") 
resolvers += Resolver.typesafeIvyRepo("releases") 

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")