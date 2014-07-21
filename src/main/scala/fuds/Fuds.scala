package fuds

import fuds.restriction._
import scala.Some
import scala.io.BufferedSource
import java.io.{File, InputStream}

object Fuds {
  def createFromBufferedSources(specifiedPort: Option[Int],
            contentWhiteList: Option[BufferedSource],
            uploadsWhiteList: Option[BufferedSource],
            https: Boolean,
            filesDirectory: String) =
    new Server(
      specifiedPort,
      contentWhiteList.map(bs => WhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveContentWhiteList),
      uploadsWhiteList.map(bs => AuthorisationWhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveAuthorisationWhiteList),
      https,
      filesDirectory
    )

  def createFromFiles(specifiedPort: Option[Int],
            contentWhiteList: Option[File],
            uploadsWhiteList: Option[File],
            https: Boolean,
            filesDirectory: String) =
    Fuds.createFromBufferedSources(
        specifiedPort,
        contentWhiteList.map(scala.io.Source.fromFile),
        uploadsWhiteList.map(scala.io.Source.fromFile),
        https,
        filesDirectory
    )

  case class Config(port: Option[Int],
                    https: Boolean,
                    filesDirectory: File,
                    contentWhiteList: Option[File],
                    uploadsWhiteList: Option[File]){
    def createFuds = Fuds.createFromFiles(port, contentWhiteList, uploadsWhiteList, https, filesDirectory.getPath)
  }

  object Parser extends scopt.OptionParser[Config]("fuds") {
    head("fuds", "1.0")
    opt[Int]("port") action { (portNumber, c) => c.copy(port = Some(portNumber))}
    opt[Unit]("https") action { (_, c) => c.copy(https = true)}
    opt[File]("storage") valueName("<directory>") action { (file, c) => c.copy(filesDirectory = file)} text("directory to store the uploaded files")
    opt[File]("content-white-list") valueName("<file>") action { (file, c) => c.copy(contentWhiteList = Some(file))}
    opt[File]("uploads-white-list") valueName("<file>") action { (file, c) => c.copy(uploadsWhiteList = Some(file))}
    help("help") text("prints this usage text")
  }

  def main(args: Array[String]) {
    val defaultConfig = Config(
      port = Some(8080),
      https = false,
      filesDirectory = new File("files/"),
      contentWhiteList = None,
      uploadsWhiteList = None
    )
    Parser.parse(args, defaultConfig).map(_.createFuds.join())
    System.exit(1)
  }
}