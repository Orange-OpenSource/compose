name := "scaas"
version := "3.1.0"
lazy val `scaas` = (project in file("."))
  .enablePlugins(PlayScala, BuildInfoPlugin, JavaAgent)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "scaasInfos",
    javaAgents += "org.mockito" % "mockito-core" % "5.14.2" % "test"
  )
scalaVersion := "3.4.2"

val web3jVersion = "4.12.2"

libraryDependencies ++= Seq(ws, specs2 % Test, guice)
dependencyOverrides += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.1"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.1"
libraryDependencies ++= Seq(
  "org.web3j" % "core" % web3jVersion,
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  // Enable reactive mongo for Play 3
  "org.reactivemongo" %% "play2-reactivemongo" % "1.1.0-play30.RC13",
  // Provide JSON serialization for reactive mongo
  "org.reactivemongo" %% "reactivemongo-play-json-compat" % "1.1.0-play30.RC13",
  // Provide BSON serialization for reactive mongo
  //"org.reactivemongo" %% "reactivemongo-bson-compat" % "0.20.13",
  // Provide JSON serialization for Joda-Time
  "org.playframework" %% "play-json-joda" % "3.0.4",
  // Bcrypt
  "org.mindrot" % "jbcrypt" % "0.4",
  // Apache validator
  "commons-validator" % "commons-validator" % "1.7",
  // Unit testing
  "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % "test",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "4.16.2" % Test,
  "de.leanovate.play-mockws" %% "play-mockws-3-0" % "3.0.3" % Test
)

javacOptions ++= Seq("-parameters")

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

Compile / unmanagedSourceDirectories := Seq(baseDirectory.value / "app")

Test / unmanagedSourceDirectories := Seq(baseDirectory.value / "test")
