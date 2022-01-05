val scala3Version = "3.1.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "excel.osm",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.typelevel" %% "cats-core" % "2.6.1",
    libraryDependencies += "org.apache.poi" % "poi" % "5.0.0",
    libraryDependencies += "org.apache.poi" % "poi-ooxml" % "5.0.0",

    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.4" % "test",    
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
