lazy val `nats-monix` = (project in file("nats-monix"))
  .settings(
    name := "scala-nats-monix-example",
    scalaVersion := "2.12.1",
    libraryDependencies ++= Seq(
      "io.nats" % "jnats" % "1.0",
      "io.monix" %% "monix-execution" % "2.2.4",
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
