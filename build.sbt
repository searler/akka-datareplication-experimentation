import AssemblyKeys._

resolvers += "patriknw at bintray" at "http://dl.bintray.com/patriknw/maven"

libraryDependencies += "com.github.patriknw" %% "akka-data-replication" % "0.7"

assemblySettings

mainClass in assembly := Some("databot.DataBot")
   
test in assembly := {}
