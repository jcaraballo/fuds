package fuds

import java.io.File

import fuds.restriction._

import scala.io.BufferedSource

object Fuds {
  def createFromBufferedSources(specifiedPort: Option[Int],
            contentWhiteList: Option[BufferedSource],
            uploadsWhiteList: Option[BufferedSource],
            keyStore: Option[(String, String)],
            filesDirectory: String,
            shouldListDirectories: Boolean) =
    new Server(
      specifiedPort,
      contentWhiteList.map(bs => WhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveContentWhiteList),
      uploadsWhiteList.map(bs => AuthorisationWhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveAuthorisationWhiteList),
      keyStore,
      filesDirectory,
      shouldListDirectories
    )

  def createFromFiles(specifiedPort: Option[Int],
            contentWhiteList: Option[File],
            uploadsWhiteList: Option[File],
            keyStore: Option[(File, String)],
            filesDirectory: String,
            shouldListDirectories: Boolean) =
    Fuds.createFromBufferedSources(
        specifiedPort,
        contentWhiteList.map(scala.io.Source.fromFile),
        uploadsWhiteList.map(scala.io.Source.fromFile),
        keyStore.map(ks => (ks._1.getPath, ks._2)),
        filesDirectory,
        shouldListDirectories
    )

  case class Config(port: Option[Int],
                    keyStore: Option[(File, Option[String])],
                    filesDirectory: File,
                    contentWhiteList: Option[File],
                    uploadsWhiteList: Option[File],
                    list: Boolean,
                    explicitlyList: Boolean,
                    explicitlyNoList: Boolean){
    def createFuds = {
      def requestPasswordFromConsole(): String = {
        print("key store password: ")
        new String(System.console().readPassword())
      }

      val ksInclPassword = keyStore.map{(ks: (File, Option[String])) =>
        (ks._1, ks._2.getOrElse(requestPasswordFromConsole()))}
      Fuds.createFromFiles(port, contentWhiteList, uploadsWhiteList, ksInclPassword, filesDirectory.getPath, list)
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
    } text("Serve HTTPS using <file> key store. If no <password> is provided, the password will be requested from the console. The default is HTTP (no HTTPS).")
    opt[File]("storage") valueName("<directory>") action { (file, c) => c.copy(filesDirectory = file)} text("Directory to store the uploaded files and to serve them for downloading.  (Default \"files/\".)")
    opt[File]("content-white-list") valueName("<file>") action { (file, c) => c.copy(contentWhiteList = Some(file))} text("White list restricting the content that can be uploaded. When no --content-white-list, any content can be uploaded. --content-white-list blank_file will reject all uploads.")
    opt[File]("uploads-white-list") valueName("<file>") action { (file, c) => c.copy(uploadsWhiteList = Some(file))} text("White list restricting which user:password credentials are allowed to upload files. When no --uploads-white-list, uploads do not require authentication. --uploads-white-list blank_file will reject all uploads.")
    opt[Unit]("list") action {(_, c) => c.copy(list = true, explicitlyList = true)} text("When GETing a url that match a directory, a list of the content will be returned (see --no-list).")
    opt[Unit]("no-list") action {(_, c) => c.copy(list = false, explicitlyNoList = true)} text("Do not allow GETing urls that match a directory (see --list).")

    help("help") text("Prints this usage text")

    checkConfig{c => if(c.explicitlyList && c.explicitlyNoList) failure("Cannot specify both --list and --no-list") else success}
  }

  def main(args: Array[String]) {
    val defaultConfig = Config(
      port = Some(8080),
      keyStore = None,
      filesDirectory = new File("files/"),
      contentWhiteList = None,
      uploadsWhiteList = None,
      list = true,
      explicitlyList = false,
      explicitlyNoList = false
    )
    Parser.parse(args, defaultConfig).map(_.createFuds.join())
    System.exit(1)
  }
}