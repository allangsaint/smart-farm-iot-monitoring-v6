ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.13.17"
allowUnsafeScalaLibUpgrade := true

lazy val root = (project in file("."))
  .settings(
    name := "smart-farm-iot-monitoring-final",
    // Aqui conecto las librerías definidas en project/Dependencies.scala
    libraryDependencies ++= Dependencies.commonDependencies ++ Dependencies.prometheusDependencies
  )