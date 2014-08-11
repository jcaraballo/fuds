import AssemblyKeys._

assemblySettings

jarName in assembly := { s"${name.value}-${version.value}.jar" }
