package acceptance

import acceptance.Helpers.{body, createdBody}
import org.scalatest.{BeforeAndAfterEach, Spec}
import fuds.{Server, Fuds}
import io.shaka.http.Request.{PUT,GET}
import io.shaka.http.{TrustAllSslCertificates, Response}
import io.shaka.http.Status.{CREATED, OK}
import io.shaka.http.Http.http

class FudsHttpsSpec extends Spec with BeforeAndAfterEach {
  object `Server must` {
    def `enable users to upload files via HTTPS`() {
      val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
      assert((response.status, body(response)) === (CREATED, Some("/fear.csv")))
    }

    def `enable users to retrieve files previously uploaded via HTTPS`() {
      assert(createdBody(http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))) === "/fear.csv")
      
      val response = http(GET(base + "/fear.csv"))
      assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
    }
  }

  override def beforeEach(){
    fuds = Fuds.createFromBufferedSources(
      specifiedPort = None,
      contentWhiteList = None,
      uploadsWhiteList = None,
      keyStore = Some(("src/test/resources/certs/keystore-local.jks", "dummypass")),
      "target/files-" + java.util.UUID.randomUUID,
      shouldListDirectories = false
    )

    TrustAllSslCertificates
  }

  override def afterEach(){
    fuds.stop()
  }

  var fuds: Server = _

  private def base = s"https://localhost:${fuds.port}"
}