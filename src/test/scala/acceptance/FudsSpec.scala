package acceptance

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import acceptance.Helpers.{body, createdBody}
import fuds.{Fuds, Server}
import io.shaka.http.Http.http
import io.shaka.http.Request.{GET, PUT}
import io.shaka.http.Response
import io.shaka.http.Status._
import org.scalatest.{BeforeAndAfterEach, Spec}

class FudsSpec extends Spec with BeforeAndAfterEach {

  object `Server must` {
    def `enable users to upload files`() {
      val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
      assert((response.status, body(response)) === (CREATED, Some("/fear.csv")))
    }

    def `enable users to retrieve files previously uploaded`() {
      assert(createdBody(http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))) === "/fear.csv")
      
      val response = http(GET(base + "/fear.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }

    def `enable users to retrieve files previously uploaded even if the server has been down in between`() {
      assert(createdBody(http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))) === "/fear.csv")

      fuds.stop()
      fuds = csvFuds(fudsDirectory)

      val response = http(GET(base + "/fear.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }

    def `allow locators of more than one path part`() {
      assert(createdBody(http(PUT(base + "/fear/uncertainty/doubt.csv").entity("foo,bar\n1,2\n"))) === "/fear/uncertainty/doubt.csv")

      val response = http(GET(base + "/fear/uncertainty/doubt.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }

    def `reject paths with any "." or ".." parts`(){
      assert(http(PUT(base + "/../fear.csv").entity("foo,bar\n1,2\n")).status === BAD_REQUEST) // This one is actually rejected by Jetty
      assert(http(PUT(base + "/fear/../fears.csv").entity("foo,bar\n1,2\n")).status === BAD_REQUEST)
      assert(http(PUT(base + "/.").entity("foo,bar\n1,2\n")).status === BAD_REQUEST)
      assert(http(PUT(base + "/./fear.csv").entity("foo,bar\n1,2\n")).status === BAD_REQUEST)
    }

    def `fail with status 404 Not found when trying to retrieve file that has never been uploaded`(){
      assert(http(GET(base + "/does-not-exist.csv")).status === NOT_FOUND)
      assert(http(GET(base + "/does/not/exist.csv")).status === NOT_FOUND)
    }

    def `restrict uploads to proper csvs where configured that way`(){
      assert(http(PUT(base + "/fear.csv").entity("foo\n1,2\n")).status === FORBIDDEN)
   }
  }

  override def beforeEach(){
    fudsDirectory = generateFudsDirectory()
    fuds = csvFuds(fudsDirectory)
  }

  private def csvFuds(filesDirectory: String): Server = Fuds.createFromBufferedSources(
    specifiedPort = None,
    contentWhiteList = Some(scala.io.Source.fromInputStream(new ByteArrayInputStream("IsCsv .*".getBytes(StandardCharsets.UTF_8)))),
    uploadsWhiteList = None,
    keyStore = None,
    filesDirectory,
    shouldListDirectories = false
  )

  private def generateFudsDirectory(): String = {
    "target/files-" + java.util.UUID.randomUUID
  }

  override def afterEach(){
    fuds.stop()
  }

  var fuds: Server = _
  var fudsDirectory: String = _

  private def base = s"http://localhost:${fuds.port}"
}