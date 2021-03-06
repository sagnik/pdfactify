organization := "edu.psu.sagnik.research"

name := "pdftables-relationaltriple"

javacOptions += "-Xlint:unchecked"

scalaVersion in ThisBuild := "2.11.8"

libraryDependencies ++= Seq(
  //for test
  "org.scalatest"  % "scalatest_2.11"             % "2.2.1"   % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck"                % "1.12.1"  % "test" withSources() withJavadoc()
)

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)


fork := true

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

testOptions in Test += Tests.Argument("-oF")

fork in Test := false

parallelExecution in Test := false
