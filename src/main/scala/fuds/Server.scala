package fuds

import unfiltered.jetty.Http
import unfiltered.request._
import unfiltered.response._
import java.nio.{file => j7file}
import unfiltered.request.Path
import scala.util.control.NonFatal
import fuds.restriction._
import java.nio.file.NoSuchFileException
import unfiltered.response.ResponseBytes
import scala.Some
import unfiltered.response.ResponseString

class Server(val maybePort: Option[Int] = None,
             val whiteList: WhiteList) {
  val port = maybePort.getOrElse(unfiltered.util.Port.any)
  var files = Map[String, Array[Byte]]()
  var httpServer: Http = _

  start()

  private def start() {
    val plan = unfiltered.filter.Planify {
      case req @ PUT(Path(resourceLocator)) =>
        try {
          val parts: Vector[String] = relativeParts(resourceLocator)

          if(parts.contains(".") || parts.contains("..")) BadRequest
          else {
            for (d <- partsToDirectoryPath(parts)) {
              if (!d.toFile.exists()) {
                val succeeded = d.toFile.mkdirs()
                if (!succeeded)
                  throw new RuntimeException(s"Failed to create directory $d")
              }
            }

            val resourceContent: Array[Byte] = Body.bytes(req)
            val canonicalResourceLocator = "/" + parts.mkString("/")

            if (whiteList(canonicalResourceLocator)(resourceContent)) {
              j7file.Files.write(partsToFullPath(parts), resourceContent)
              ResponseString(canonicalResourceLocator)
            } else Forbidden
          }
        } catch {
          case NonFatal(e) => e.printStackTrace() ; throw e
        }

      case GET(Path(resourceLocator)) =>
        try {
          ResponseBytes(j7file.Files.readAllBytes(partsToFullPath(relativeParts(resourceLocator))))
        }
        catch {
          case _: NoSuchFileException => NotFound
        }
    }

    httpServer = unfiltered.jetty.Http(port).filter(plan).start()

    println("Server started on port " + port)
  }

  private def partsToDirectoryPath(parts: Vector[String]): Option[j7file.Path] =
    if (parts.size>1) Some(j7file.Paths.get(parts.dropRight(1).mkString(java.io.File.separator)))
    else None

  private def partsToFullPath(parts: Vector[String]): j7file.Path = j7file.Paths.get(parts.mkString(java.io.File.separator))
  private def relativeParts(resourceLocator: String): Vector[String] = resourceLocator.dropWhile(_=='/').split("/").toVector

  def stop() {
    httpServer.stop()
  }

  def join() {
    httpServer.join()
  }
}