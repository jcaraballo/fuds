package acceptance

import org.scalatest.{BeforeAndAfterEach, Spec}
import fuds.Server
import io.shaka.http.Http.http
import io.shaka.http.Request.{PUT,GET}
import io.shaka.http.Response
import io.shaka.http.Status.{FORBIDDEN, NOT_FOUND, OK, BAD_REQUEST}
import fuds.restriction.{PathRegexWhiteList, IsCsv}

class FudsSpec extends Spec with BeforeAndAfterEach {

  object `Server must` {
    def `enable users to upload files`() {
      val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
      assert((response.status, body(response)) === (OK, Some("/fear.csv")))
    }

    def `enable users to retrieve files previously uploaded`() {
      assert(okBody(http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))) === "/fear.csv")
      
      val response = http(GET(base + "/fear.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }

    def `enable users to retrieve files previously uploaded even if the server has been down in between`() {
      assert(okBody(http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))) === "/fear.csv")

      server.stop()
      server = csvFuds().start()

      val response = http(GET(base + "/fear.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }

    def `allow locators of more than one path part`() {
      assert(okBody(http(PUT(base + "/fear/uncertainty/doubt.csv").entity("foo,bar\n1,2\n"))) === "/fear/uncertainty/doubt.csv")

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

    def `fail with appropriate status codes`(){
      pending
    }

    def `limit the allowed size`(){
      pending
    }

    def `restrict uploads to proper csvs where configured that way`(){
      assert(http(PUT(base + "/fear.csv").entity("foo\n1,2\n")).status === FORBIDDEN)
   }
  }

  private def body(response: Response): Option[String] = response.entity.map(_.toString().trim)
  private def okBody(response: Response): String = {
    assert(response.status === OK)
    response.entityAsString.trim
  }

  override def beforeEach(){
    server = csvFuds().start()
  }

  def csvFuds(): Server = {
    new Server(None, new PathRegexWhiteList(".*".r, IsCsv))
  }

  override def afterEach(){
    server.stop()
  }

  var server: Server = _

  private def base = s"http://localhost:${server.port}"
}