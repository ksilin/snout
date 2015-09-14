organization := "com.example"

version := "0.1"

scalaVersion := "2.11.7"

enablePlugins(JavaAppPackaging)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

scalacOptions in Test ++= Seq("-Yrangepos")

javaOptions := Seq(
  "-d64",
  "-server", // redundant - already implicitly included in d64
  "-XX:+TieredCompilation", // redundant, enabled per default
  "-XX:+AggressiveOpts",
  "-XX:+DoEscapeAnalysis", // redundant - enabled by defualt
  "-XX:+UseCompressedOops", // redundant - enabled by default if heap < 32G
  "-XX:NewRatio=1", // old/new - default is 2
  "-XX:+UseStringDeduplication",
  "-XX:+PrintGC",
  "-XX:+PrintGCDetails",
  "-XX:+PrintGCDateStamps",
  "-XX:+PrintReferenceGC",
  "-XX:+PrintGCCause",
  "-XX:+PrintPromotionFailure",
  "-XX:+PrintTenuringDistribution",
  "-XX:+PrintStringDeduplicationStatistics",
  "-XX:+HeapDumpOnOutOfMemoryError",
  "-Xloggc:./gc.log",
  "-XX:+UseGCLogFileRotation",
  "-XX:GCLogFileSize=256m",
  "-XX:NumberOfGCLogFiles=5",
  "-XX:+PrintGCApplicationStoppedTime",
  "-XX:+PrintGCApplicationConcurrentTime",
  "-XX:+AggressiveHeap" // enables heap optimization - best for long-runnign jobs with much allocation
)

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases"

libraryDependencies ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.3"
  Seq(
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "io.spray" %% "spray-json" % "1.3.1",
    "io.spray" %% "spray-testkit" % sprayV % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "org.specs2" %% "specs2-core" % "2.4.17" % "test",
    "com.jsuereth" %% "scala-arm" % "1.4",
    "mysql" % "mysql-connector-java" % "5.1.6",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "org.codehaus.janino" % "janino" % "2.7.8",
    "com.typesafe.play" % "anorm_2.11" % "2.4.0",
    "org.scalikejdbc" % "scalikejdbc_2.11" % "2.2.7",
    "com.chuusai" %% "shapeless" % "2.2.5",
    "org.scalaz" %% "scalaz-core" % "7.1.3"
  )
}


Revolver.settings

