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
    allenaiconversion,
    figuretableextractionSVG,
    pdftabletorelationaltriple
  )
  .settings(publishArtifact := false)

lazy val pdffigures = project
  .in(file("pdffigures2"))
  .settings(publishArtifact := false)
  .settings(test in assembly := {})

lazy val allenaiconversion = project
  .in(file("allenaiconversion"))
  .dependsOn(pdffigures)
  .settings(publishArtifact := false)
  .settings(test in assembly := {})

lazy val figuretableextractionSVG = project
  .in(file("figuretableextractionSVG"))
  .dependsOn(allenaiconversion)
  .settings(publishArtifact := false)
  .settings(
    mainClass in assembly := Some("edu.psu.sagnik.research.figuretableextractionSVG.CreateFigureTables"),
    test in assembly := {}
  )

lazy val pdftabletorelationaltriple  = project
  .in(file("pdftabletorelationaltriple"))
  .dependsOn(allenaiconversion)
  .settings(publishArtifact := false)
  .settings(test in assembly := {})

lazy val subprojects: Seq[ProjectReference] = root.aggregate
lazy val publishTasks = subprojects.map{ r => publish.in(r) }
