val CatsVersion = "2.9.0"
val CatsEffectVersion = "3.4.9"
val CatsEffectTestKitVersion = "3.4.7"
val WeaverCatsVersion = "0.8.3"
val Http4sVersion = "0.23.18"

val Scala3 = "3.2.2"

ThisBuild / crossScalaVersions := Seq(Scala3)
ThisBuild / scalaVersion := Scala3

ThisBuild / organization := "io.github.rzqx"
ThisBuild / organizationName := "rzqx-scala"

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

ThisBuild / licenses := Seq("APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/rzqx-scala/formed3"))
ThisBuild / developers := List(
  Developer("rzqx", "Melvin Low", "me@melvinlow.com", url("https://melvinlow.com"))
)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"

usePgpKeyHex("821A82C15670B776F9950C8046E96DBCFD1E8107")

lazy val core = (project in file("."))
  .settings(
    name := "formed3",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % CatsVersion,
      "org.typelevel" %% "cats-effect" % CatsEffectVersion,
      "com.disneystreaming" %% "weaver-cats" % WeaverCatsVersion % Test
    ),
    testFrameworks ++= List(
      new TestFramework("weaver.framework.CatsEffect")
    )
  )

lazy val docs = project
  .in(file("formed-docs"))
  .dependsOn(core)
  .enablePlugins(MdocPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % Http4sVersion
    ),
    mdocIn := (ThisBuild / baseDirectory).value / "docs" / "src",
    mdocOut := (ThisBuild / baseDirectory).value / "docs" / "out"
  )
