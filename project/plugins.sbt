resolvers += Resolver.typesafeRepo("eleases") 
resolvers += Resolver.typesafeIvyRepo("releases") 

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.0")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")