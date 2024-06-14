name := """PlayChat"""

version := "2.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  // Enable reactive mongo for Play 2.8
  "org.reactivemongo" %% "play2-reactivemongo" % "0.20.13-play28",
  // Provide JSON serialization for reactive mongo
  "org.reactivemongo" %% "reactivemongo-play-json-compat" % "0.20.13-play28",
  // Provide BSON serialization for reactive mongo
  "org.reactivemongo" %% "reactivemongo-bson-compat" % "0.20.13",
  // Provide JSON serialization for Joda-Time
  "com.typesafe.play" %% "play-json-joda" % "2.7.4",
  "com.nimbusds" % "nimbus-jose-jwt" % "9.40"
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

play.sbt.routes.RoutesKeys.routesImport +=
"play.modules.reactivemongo.PathBindables._"

// Setting idle timeout to 5 minutes
PlayKeys.devSettings += "play.server.http.idleTimeout" -> "360000"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
