package fuds

import fuds.restriction._
import scala.Some
import scala.io.BufferedSource
import java.io.{File, InputStream}

object Fuds {
  def createFromBufferedSources(specifiedPort: Option[Int],
            contentWhiteList: Option[BufferedSource],
            uploadsWhiteList: Option[BufferedSource],
            keyStore: Option[(String, String)],
            filesDirectory: String) =
    new Server(
      specifiedPort,
      contentWhiteList.map(bs => WhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveContentWhiteList),
      uploadsWhiteList.map(bs => AuthorisationWhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveAuthorisationWhiteList),
      keyStore,
      filesDirectory
    )

  def createFromFiles(specifiedPort: Option[Int],
            contentWhiteList: Option[File],
            uploadsWhiteList: Option[File],
            keyStore: Option[(File, String)],
            filesDirectory: String) =
    Fuds.createFromBufferedSources(
        specifiedPort,
        contentWhiteList.map(scala.io.Source.fromFile),
        uploadsWhiteList.map(scala.io.Source.fromFile),
        keyStore.map(ks => (ks._1.getPath, ks._2)),
        filesDirectory
    )

  case class Config(port: Option[Int],
                    keyStoreLocation: Option[File],
                    keyStorePassword: Option[String],
                    filesDirectory: File,
                    contentWhiteList: Option[File],
                    uploadsWhiteList: Option[File]){
    def createFuds = {
      val keyStore: Option[(File, String)] = (keyStoreLocation, keyStorePassword) match {
        case (None, None) => None
        case (Some(location), Some(password)) => Some((location, password))
        case (None, Some(_)) => throw new IllegalArgumentException("Key store password provided, but not its location")
        case (Some(location), None) =>
          print("key store password: ")
          val password = new String(System.console().readPassword())
          Some((location, password))
      }
      Fuds.createFromFiles(port, contentWhiteList, uploadsWhiteList, keyStore, filesDirectory.getPath)
    }
  }

  object Parser extends scopt.OptionParser[Config]("fuds") {
    head("fuds", "1.0")

    opt[Int]("port") action { (portNumber, c) => c.copy(port = Some(portNumber))}
    opt[File]("key-store") valueName("<file>") action { (file, c) => c.copy(keyStoreLocation = Some(file))} text("Key store for HTTPS. Fuds will serve http by default, or https if --key-store is provided. If there's no --key-store-password the password will be requested from the console.")
    opt[String]("key-store-password") valueName("<password>") action { (pw, c) => c.copy(keyStorePassword = Some(pw))} text("Password to access the store and the key (requires --key-store)")
    opt[File]("storage") valueName("<directory>") action { (file, c) => c.copy(filesDirectory = file)} text("Directory to store the uploaded files and to serve them for downloading.  (Default \"files/\".)")
    opt[File]("content-white-list") valueName("<file>") action { (file, c) => c.copy(contentWhiteList = Some(file))} text("White list restricting the content that can be uploaded. When no --content-white-list, any content can be uploaded. --content-white-list blank_file will reject all uploads.")
    opt[File]("uploads-white-list") valueName("<file>") action { (file, c) => c.copy(uploadsWhiteList = Some(file))} text("White list restricting which user:password credentials are allowed to upload files. When no --uploads-white-list, uploads do not require authentication. --uploads-white-list blank_file will reject all uploads.")

    help("help") text("Prints this usage text")
  }

  def main(args: Array[String]) {
    val defaultConfig = Config(
      port = Some(8080),
      keyStoreLocation = None,
      keyStorePassword = None,
      filesDirectory = new File("files/"),
      contentWhiteList = None,
      uploadsWhiteList = None
    )
    Parser.parse(args, defaultConfig).map(_.createFuds.join())
    System.exit(1)
  }
}