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
                    keyStore: Option[(File, Option[String])],
                    filesDirectory: File,
                    contentWhiteList: Option[File],
                    uploadsWhiteList: Option[File]){
    def createFuds = {
      def requestPasswordFromConsole(): String = {
        print("key store password: ")
        new String(System.console().readPassword())
      }

      val ksInclPassword = keyStore.map{(ks: (File, Option[String])) =>
        (ks._1, ks._2.getOrElse(requestPasswordFromConsole()))}
      Fuds.createFromFiles(port, contentWhiteList, uploadsWhiteList, ksInclPassword, filesDirectory.getPath)
    }
  }

  object Parser extends scopt.OptionParser[Config]("fuds") {
    head("fuds", "1.0")

    opt[Int]("port") action { (portNumber, c) => c.copy(port = Some(portNumber))}
    opt[String]("https") valueName("<file>[:<password>]") action { (fileAndPassword: String, c: Config) =>
      val separatorIndex: Int = fileAndPassword.indexOf(":")
      if(separatorIndex == -1) {
        val file = new File(fileAndPassword)
        c.copy(keyStore = Some((file, None)))
      } else {
        val file = new File(fileAndPassword.substring(0, separatorIndex))
        val password = fileAndPassword.substring(separatorIndex+1)
        c.copy(keyStore = Some((file, Some(password))))
      }
    } text("Key store for HTTPS. Fuds will serve http by default, or https if --key-store is provided. If there's no --key-store-password the password will be requested from the console.")
    opt[File]("storage") valueName("<directory>") action { (file, c) => c.copy(filesDirectory = file)} text("Directory to store the uploaded files and to serve them for downloading.  (Default \"files/\".)")
    opt[File]("content-white-list") valueName("<file>") action { (file, c) => c.copy(contentWhiteList = Some(file))} text("White list restricting the content that can be uploaded. When no --content-white-list, any content can be uploaded. --content-white-list blank_file will reject all uploads.")
    opt[File]("uploads-white-list") valueName("<file>") action { (file, c) => c.copy(uploadsWhiteList = Some(file))} text("White list restricting which user:password credentials are allowed to upload files. When no --uploads-white-list, uploads do not require authentication. --uploads-white-list blank_file will reject all uploads.")

    help("help") text("Prints this usage text")
  }

  def main(args: Array[String]) {
    val defaultConfig = Config(
      port = Some(8080),
      keyStore = None,
      filesDirectory = new File("files/"),
      contentWhiteList = None,
      uploadsWhiteList = None
    )
    Parser.parse(args, defaultConfig).map(_.createFuds.join())
    System.exit(1)
  }
}