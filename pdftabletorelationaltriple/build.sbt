organization := "edu.psu.sagnik.research"

name := "pdftables-relationaltriple"

javacOptions += "-Xlint:unchecked"

scmInfo := Some(ScmInfo(
  url("https://github.com/sagnik/pdftabletorelationaltriple"),
  "https://github.com/allenai/pdftabletorelationaltriple"))
   
libraryDependencies ++= Seq(
  //pdfparser for graphics paths
  "edu.psu.sagnik.research" %% "pdsimplifyparser" % "0.0.5" exclude("javax.jms", "jms") exclude("com.sun.jdmk", "jmxtools") exclude("com.sun.jmx", "jmxri"),
  //jackson for json
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  //breeze for algebra
  //"org.scalanlp" %% "breeze" % "0.11.2",
  //"org.scalanlp" %% "breeze-natives" % "0.11.2",
  //"org.scalanlp" %% "breeze-viz" % "0.11.2",
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
