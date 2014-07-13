package fuds

import fuds.restriction._
import scala.Some
import scala.io.BufferedSource
import java.io.InputStream

class Fuds private (private val specifiedPort: Option[Int], private val whiteList: BufferedSource){
  private val server = new Server(specifiedPort, WhiteListParser.parse(whiteList.getLines().toList))

  def join(): Fuds = {server.join(); this}
  def stop(){server.stop()}
  def port: Int = server.port
}

object Fuds {
  def apply(port: Int, whiteListInputStream: InputStream): Fuds = new Fuds(Some(port), scala.io.Source.fromInputStream(whiteListInputStream))

  def apply(port: Int, whiteListFilePath: String): Fuds = new Fuds(Some(port), scala.io.Source.fromFile(whiteListFilePath))

  def apply(whiteListInputStream: InputStream): Fuds = new Fuds(None, scala.io.Source.fromInputStream(whiteListInputStream))

  def main(args: Array[String]) {
    if(args.size==0) Fuds(8080, ClassLoader.getSystemResourceAsStream("default.white_list"))
    else if(args.size==2) Fuds(port = args(0).toInt, whiteListFilePath = args(1))
    else System.err.println("Failed to start fuds. Usage: fuds <port> <white_list_file_location>")
  }
}