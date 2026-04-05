import sbt._

object Dependencies {
  object Versions {
    val spark      = "4.1.1"
    val delta      = "4.1.0"
    val cats       = "2.13.0"
    val prometheus = "0.16.0"
    val config     = "1.4.6"
    val scalaTest  = "3.2.19"
  }

  // Grupo de librerías de Prometheus
  val prometheusDependencies: Seq[ModuleID] = Seq(
    "io.prometheus" % "simpleclient"            % Versions.prometheus,
    "io.prometheus" % "simpleclient_hotspot"    % Versions.prometheus,
    "io.prometheus" % "simpleclient_httpserver" % Versions.prometheus,
    "io.prometheus" % "simpleclient_dropwizard" % Versions.prometheus
  )

  // Librerías base del proyecto
  val commonDependencies: Seq[ModuleID] = Seq(
    "org.apache.spark" %% "spark-sql"            % Versions.spark % "provided",
    "org.apache.spark" %% "spark-sql-kafka-0-10" % Versions.spark,
    "io.delta"         %% "delta-spark"          % Versions.delta,
    "org.typelevel"    %% "cats-core"            % Versions.cats,
    "com.typesafe"      % "config"               % Versions.config,
    "org.scalatest"    %% "scalatest"            % Versions.scalaTest % Test
  )
}