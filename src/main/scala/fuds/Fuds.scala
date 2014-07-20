package fuds

import fuds.restriction._
import scala.Some
import scala.io.BufferedSource
import java.io.{File, InputStream}

object Fuds {
  def createFromBufferedSources(specifiedPort: Option[Int],
            contentWhiteList: Option[BufferedSource],
            uploadsWhiteList: Option[BufferedSource],
            https: Boolean) =
    new Server(
      specifiedPort,
      contentWhiteList.map(bs => WhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveContentWhiteList),
      uploadsWhiteList.map(bs => AuthorisationWhiteListParser.parse(bs.getLines().toList))
        .getOrElse(PermissiveAuthorisationWhiteList),
      https
    )

  def createFromFiles(specifiedPort: Option[Int],
            contentWhiteList: Option[File],
            uploadsWhiteList: Option[File],
            https: Boolean) =
    Fuds.createFromBufferedSources(
        specifiedPort,
        contentWhiteList.map(scala.io.Source.fromFile),
        uploadsWhiteList.map(scala.io.Source.fromFile),
        https
    )

  case class Config(port: Option[Int],
                    https: Boolean,
                    contentWhiteList: Option[File],
                    uploadsWhiteList: Option[File]){
    def createFuds = Fuds.createFromFiles(port, contentWhiteList, uploadsWhiteList, https)
  }

  object Parser extends scopt.OptionParser[Config]("fuds") {
    head("fuds", "1.0")
    opt[Int]("port") abbr("p") action { (portNumber, c) => c.copy(port = Some(portNumber))}
    opt[Unit]("https") abbr("s") action { (_, c) => c.copy(https = true)}
    opt[File]("content-white-list") abbr("c") valueName("<file>") action { (file, c) => c.copy(contentWhiteList = Some(file))}
    opt[File]("uploads-white-list") abbr("u") valueName("<file>") action { (file, c) => c.copy(uploadsWhiteList = Some(file))}
    help("help") abbr("h") text("prints this usage text")
  }

  def main(args: Array[String]) {
    val defaultConfig = Config(
      port = Some(8080),
      https = false,
      contentWhiteList = None,
      uploadsWhiteList = None
    )
    Parser.parse(args, defaultConfig).map(_.createFuds.join())
    System.exit(1)
  }
}