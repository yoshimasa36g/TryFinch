name := "TryFinch"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val finchVersion = "0.17.0"
  Seq(
    "com.github.finagle" %% "finch-core" % finchVersion,
    "com.github.finagle" %% "finch-circe" % finchVersion,
    "io.circe" %% "circe-generic" % "0.9.0",
    "com.typesafe" % "config" % "1.3.2",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "io.getquill" %% "quill-finagle-mysql" % "2.3.2",
    "org.springframework.security" % "spring-security-web" % "5.0.3.RELEASE",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  )
}
