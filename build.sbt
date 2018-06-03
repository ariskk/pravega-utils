name := "pravega-source"

version := "0.1"

scalaVersion := "2.11.12"

lazy val flinkVersion = "1.4.2"
lazy val flinkLibs = Seq(
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
  "org.apache.flink" %% "flink-test-utils" % flinkVersion,
  "org.apache.flink" %% "flink-streaming-contrib" % flinkVersion,
  "org.apache.flink" %% "flink-queryable-state-client-java" % flinkVersion
)

lazy val circeVersion = "0.9.3"
lazy val circeLibs = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.chuusai" %% "shapeless" % "2.3.3",
  "org.typelevel" %% "cats-core" % "1.1.0",
  "io.monix" %% "monix" % "3.0.0-RC1",
  "io.pravega" %% "pravega-connectors-flink" % "0.2.1",
  "org.slf4j" % "slf4j-simple" % "1.6.2" % Test
) ++ flinkLibs ++ circeLibs

scalacOptions ++= Seq("-Ypartial-unification", "-target:jvm-1.8")

enablePlugins(DockerComposePlugin)
