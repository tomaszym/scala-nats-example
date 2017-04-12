lazy val root = (project in file("."))
  .settings(
    name := "scala-nats-monix-example",
    scalaVersion := "2.12.1",
    libraryDependencies ++= Seq(
      "io.nats" % "jnats" % "1.0",
      "io.monix" %% "monix" % "2.2.4",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
    )
  )
