package acceptance

import acceptance.Helpers.{createdBody, okBody}
import fuds.{Fuds, Server}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.CONTENT_TYPE
import io.shaka.http.Request.{GET, PUT}
import io.shaka.http.Status.NOT_FOUND
import org.scalatest.{BeforeAndAfterEach, Spec}

import scala.xml.XML

class FudsDirectoryListingSpec extends Spec with BeforeAndAfterEach {

  object `Server must` {
    def `list directory contents when set up to do so`() {
      fuds = createFuds(shouldListDirectories = true)

      createdBody(http(PUT(base + "/components/fear.csv").entity("booh")))
      createdBody(http(PUT(base + "/components/uncertainty.csv").entity("eeh?")))
      createdBody(http(PUT(base + "/components/doubt.csv").entity("could be")))

      val response = http(GET(base + "/components/"))
      assert(response.headers(CONTENT_TYPE) === List("text/html;charset=UTF-8"))
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

      createdBody(http(PUT(base + "/components/fear.csv").entity("booh")))
      createdBody(http(PUT(base + "/components/more/uncertainty.csv").entity("eeh?")))

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

      createdBody(http(PUT(base + "/components/fear.csv").entity("booh")))
      createdBody(http(PUT(base + "/components/uncertainty.csv").entity("eeh?")))
      createdBody(http(PUT(base + "/components/doubt.csv").entity("could be")))

      val response = http(GET(base + "/components/"))
      assert(response.status === NOT_FOUND)
    }
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