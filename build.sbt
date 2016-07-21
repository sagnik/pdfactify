version in ThisBuild := {
  val major = 0
  val minor = 0
  val patch = 1
  s"$major.$minor.$patch"
} 

scalaVersion in ThisBuild := "2.11.8"

javacOptions += "-Xlint:unchecked"

organization := "edu.psu.sagnik.research"

name := "pdfactify"

lazy val root = project
  .in(file("."))
  .aggregate(
    pdffigures, 
    pdftabletorelationaltriple
 )
  .settings(publishArtifact := false)

lazy val pdffigures = project
  .in(file("pdffigures2"))
  //.enablePlugins(WebServicePlugin)
  .settings(publishArtifact := false)

lazy val pdftabletorelationaltriple  = project
  .in(file("pdftabletorelationaltriple"))
  .dependsOn(pdffigures)
  .settings(publishArtifact := false)

lazy val subprojects: Seq[ProjectReference] = root.aggregate
lazy val publishTasks = subprojects.map{ r => publish.in(r) }
