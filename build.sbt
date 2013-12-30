import AssemblyKeys._

//import com.typesafe.startscript.StartScriptPlugin

name := "delsym"

version := "0.1"

organization := "com.mycompany"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases",
  "Sonatype snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Spray Repository"    at "http://repo.spray.io",
  "Spray Nightlies"     at "http://nightlies.spray.io")

libraryDependencies ++= {
  val akkaVersion = "2.2.3"
  val sprayVersion = "1.2.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor"      % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"      % akkaVersion,
    "com.typesafe.akka" %% "akka-remote"     % akkaVersion,
    "io.spray"          %  "spray-can"       % sprayVersion,
    "io.spray"          %  "spray-routing"   % sprayVersion,
    "io.spray"          %% "spray-json"      % "1.2.5",
    "org.json4s"        %% "json4s-native"   % "3.2.4",
    "commons-codec"     %  "commons-codec"   % "1.5",
    "commons-httpclient" % "commons-httpclient" % "3.1",
    "org.mongodb"       %% "casbah"          % "2.5.0",
    "org.apache.tika"   %  "tika-core"       % "1.4",
    "org.apache.tika"   %  "tika-parsers"    % "1.4",
    "org.apache.solr"   %  "solr-solrj"      % "4.6.0",
    "ch.qos.logback"    %  "logback-classic" % "1.0.10",
    "com.typesafe.akka" %% "akka-testkit"    % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",
    "org.scalatest"     %% "scalatest"       % "1.9.1"     % "test"
  )
}

// Assembly settings
mainClass in Global := Some("com.mycompany.Main")

jarName in assembly := "delsym-server.jar"

assemblySettings

//seq(StartScriptPlugin.startScriptForClassesSettings: _*)

