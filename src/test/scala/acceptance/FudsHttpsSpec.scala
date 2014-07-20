package acceptance

import org.scalatest.{BeforeAndAfterEach, Spec}
import fuds.Fuds
import io.shaka.http.Request.{PUT,GET}
import io.shaka.http.{TrustAllSslCertificates, Response}
import io.shaka.http.Status.OK
import io.shaka.http.Http.http

class FudsHttpsSpec extends Spec with BeforeAndAfterEach {
  object `Server must` {
    def `enable users to upload files via HTTPS`() {
      val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
      assert((response.status, body(response)) === (OK, Some("/fear.csv")))
    }

    def `enable users to retrieve files previously uploaded via HTTPS`() {
      assert(okBody(http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))) === "/fear.csv")
      
      val response = http(GET(base + "/fear.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }
  }

  private def body(response: Response): Option[String] = response.entity.map(_.toString().trim)
  private def okBody(response: Response): String = {
    assert(response.status === OK)
    response.entityAsString.trim
  }

  override def beforeEach(){
    fuds = Fuds.startHttps(None)
    TrustAllSslCertificates
  }

  override def afterEach(){
    fuds.stop()
  }

  var fuds: Fuds = _

  private def base = s"https://localhost:${fuds.port}"
}