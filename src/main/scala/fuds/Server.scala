package fuds

import unfiltered.jetty.Https
import unfiltered.request._
import unfiltered.response._
import java.nio.{file => j7file}
import unfiltered.request.Path
import scala.util.control.NonFatal
import fuds.restriction._
import unfiltered.response.ResponseBytes
import unfiltered.response.ResponseString
import unfiltered.Cycle
import org.eclipse.jetty.server.Response
import java.util.Date

class Server(val maybePort: Option[Int] = None,
             val contentWhiteList: ContentWhiteList,
             val uploadsWhiteList: AuthorisationWhiteList,
             val keyStore: Option[(String, String)],
             val filesDirectory: String,
             val shouldListDirectories: Boolean = true) {
  val port = maybePort.getOrElse(unfiltered.util.Port.any)
  var files = Map[String, Array[Byte]]()
  var unfilteredServer: unfiltered.jetty.Server = _
  val baseDirectory: String =
    if(filesDirectory.endsWith(java.io.File.separator)) filesDirectory
    else filesDirectory + java.io.File.separator

  start()

  object HttpMethod {
    def unapply[T](req: HttpRequest[T]): Option[String] = Some(req.method.toUpperCase)
  }

  object UploadsAuth {
    def apply[A, B](intent: Cycle.Intent[A, B]) =
      Cycle.Intent[A, B] {
        case req @ HttpMethod(method) if method != "PUT" =>
          Cycle.Intent.complete(intent)(req)
        case req@BasicAuth(user, pass) if uploadsWhiteList(Some(user, pass)) =>
          Cycle.Intent.complete(intent)(req)
        case req: HttpRequest[A] if req.headers("Authorization").isEmpty && uploadsWhiteList(None) =>
          Cycle.Intent.complete(intent)(req)
        case _ =>
          // We could give the path regex here, but I'm not sure it would be a good idea
          Unauthorized ~> WWWAuthenticate( """Basic realm="/"""")
      }
  }

    object RequestLogging {
      def apply[A, B](intent: Cycle.Intent[A, B]) =
        Cycle.Intent[A, B] {
          case req =>
            val startTime = System.currentTimeMillis()
            Cycle.Intent.complete(intent)(req) ~> new ResponseFunction[Any]() {
              override def apply[C <: Any](resp: HttpResponse[C]) = {
                val responseTime = System.currentTimeMillis() - startTime
                println(s"${req.remoteAddr} ${new Date()} ${req.method} ${req.uri} ${resp.underlying.asInstanceOf[Response].getStatus} $responseTime")
                resp
              }
            }
        }
    }

  private def start() {
    val plan = new unfiltered.filter.Plan {
      def intent = RequestLogging { UploadsAuth {
        case req @ PUT(Path(resourceLocator)) =>
          try {
            val parts: Vector[String] = relativeParts(resourceLocator)

            if(parts.contains(".") || parts.contains("..")) BadRequest
            else {
              val resourceContent: Array[Byte] = Body.bytes(req)
              val canonicalResourceLocator = "/" + parts.mkString("/")

              if (contentWhiteList(canonicalResourceLocator)(resourceContent)) {
                val directory =  partsToDirectoryPath(parts)
                if (directory.getNameCount>=1 && !directory.toFile.exists()) {
                  val succeeded = directory.toFile.mkdirs()
                  if (!succeeded)
                    throw new RuntimeException(s"Failed to create directory $directory")
                }

                j7file.Files.write(partsToFullPath(parts), resourceContent)

                ResponseString(canonicalResourceLocator)
              } else Forbidden
            }
          } catch {
            case NonFatal(e) => e.printStackTrace() ; throw e
          }

        case GET(Path(resourceLocator)) =>
          try {
            val urlPathParts: Vector[String] = relativeParts(resourceLocator)
            val fullPath: j7file.Path = partsToFullPath(urlPathParts)
            if (j7file.Files.isDirectory(fullPath)) {
              if(shouldListDirectories) htmlListing(fullPath, urlPathParts) else NotFound
            } else ResponseBytes(j7file.Files.readAllBytes(fullPath))
          }
          catch {
            case _: j7file.NoSuchFileException => NotFound
          }
      }}
    }

    unfilteredServer = keyStore match {
      case None => unfiltered.jetty.Http(port).filter(plan).start()
      case Some((location, password)) =>
        new Https(port, "0.0.0.0"){
          override lazy val keyStore: String = location
          override lazy val keyStorePassword: String = password
        }.filter(plan).start()
    }

    println("Server started on port " + port)
  }

  private def partsToDirectoryPath(parts: Vector[String]): j7file.Path =
    j7file.Paths.get(baseDirectory + parts.dropRight(1).mkString(java.io.File.separator))

  private def partsToFullPath(parts: Vector[String]): j7file.Path = 
    j7file.Paths.get(baseDirectory + parts.mkString(java.io.File.separator))
  private def relativeParts(resourceLocator: String): Vector[String] = resourceLocator.dropWhile(_=='/').split("/").toVector

  private def htmlListing(fullPath: j7file.Path, urlPathParts: Vector[String]) = {
    val listing =
      <html lang="en">
        <head>
          <meta charset="utf-8"/>
          <title>title</title>
        </head>
        <body>
          <table id="listing"> {
            val directoryStream = j7file.Files.newDirectoryStream(fullPath)
            try {
              import scala.collection.convert.WrapAsScala.iterableAsScalaIterable
              directoryStream.toSeq.sortBy{_.getFileName}.map{ p =>
                <tr>
                  <td><a href={"/" + urlPathParts.mkString("/") + "/" + p.getFileName.toString}>{p.getFileName.toString}</a></td>
                </tr>
              }
            } finally {
              directoryStream.close()
            }
          } </table>
        </body>
      </html>
    Html5(listing)
  }

  def stop() {
    unfilteredServer.stop()
  }

  def join() {
    unfilteredServer.join()
  }
}