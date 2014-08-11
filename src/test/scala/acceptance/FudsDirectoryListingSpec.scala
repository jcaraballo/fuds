package acceptance

import fuds.{Fuds, Server}
import io.shaka.http.Http.http
import io.shaka.http.Request.{GET, PUT}
import io.shaka.http.Response
import io.shaka.http.Status.{NOT_FOUND, OK}
import org.scalatest.{BeforeAndAfterEach, Spec}

import scala.xml.XML

class FudsDirectoryListingSpec extends Spec with BeforeAndAfterEach {

  object `Server must` {
    def `list directory contents when set up to do so`() {
      fuds = createFuds(shouldListDirectories = true)

      okBody(http(PUT(base + "/components/fear.csv").entity("booh")))
      okBody(http(PUT(base + "/components/uncertainty.csv").entity("eeh?")))
      okBody(http(PUT(base + "/components/doubt.csv").entity("could be")))

      val response = http(GET(base + "/components/"))
      val page = XML.loadString(okBody(response).split("\n").drop(1).mkString("\n"))

      assert((page \\ "table" \ "tr" \ "td" \ "a").map{link =>
        val linkUrl = (link \ "@href").text
        val linkText = link.text
        (linkUrl, linkText)
      } === Seq(
        // Sorted by filename
        ("/components/doubt.csv", "doubt.csv"),
        ("/components/fear.csv", "fear.csv"),
        ("/components/uncertainty.csv", "uncertainty.csv")
      ))
    }

    def `include subdirectories in directory listing`(){
      fuds = createFuds(shouldListDirectories = true)

      okBody(http(PUT(base + "/components/fear.csv").entity("booh")))
      okBody(http(PUT(base + "/components/more/uncertainty.csv").entity("eeh?")))

      val response = http(GET(base + "/components/"))
      val page = XML.loadString(okBody(response).split("\n").drop(1).mkString("\n"))

      assert((page \\ "table" \ "tr" \ "td" \ "a").map{link =>
        val linkUrl = (link \ "@href").text
        val linkText = link.text
        (linkUrl, linkText)
      } === Seq(
        // Sorted by filename
        ("/components/fear.csv", "fear.csv"),
        ("/components/more", "more")
      ))
    }

    def `not list directory contents when set up to not list directory contents`() {
      fuds = createFuds(shouldListDirectories = false)

      okBody(http(PUT(base + "/components/fear.csv").entity("booh")))
      okBody(http(PUT(base + "/components/uncertainty.csv").entity("eeh?")))
      okBody(http(PUT(base + "/components/doubt.csv").entity("could be")))

      val response = http(GET(base + "/components/"))
      assert(response.status === NOT_FOUND)
    }
  }

  private def body(response: Response): Option[String] = response.entity.map(_.toString().trim)
  private def okBody(response: Response): String = {
    assert(response.status === OK)
    response.entityAsString.trim
  }

  private def generateFudsDirectory(): String = {
    "target/files-" + java.util.UUID.randomUUID
  }

  private def createFuds(shouldListDirectories: Boolean) = {
    Fuds.createFromBufferedSources(
      specifiedPort = None,
      contentWhiteList = None,
      uploadsWhiteList = None,
      keyStore = None,
      fudsDirectory,
      shouldListDirectories
    )
  }

  override def beforeEach(){
    fudsDirectory = generateFudsDirectory()
  }

  override def afterEach(){
    fuds.stop()
  }

  var fuds: Server = _
  var fudsDirectory: String = _

  private def base = s"http://localhost:${fuds.port}"
}