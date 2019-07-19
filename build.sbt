name := "vodmvi-ads"

version := "1.0"

lazy val `vodmvi-ads` = (project in file(".")).enablePlugins(PlayJava)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(javaJdbc, cache, javaWs, guice)
libraryDependencies += "com.google.api-ads" % "google-ads" % "4.0.0"
libraryDependencies += "joda-time" % "joda-time" % "2.10.3"



unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")

      