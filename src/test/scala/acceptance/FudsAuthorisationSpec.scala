package acceptance

import org.scalatest.{BeforeAndAfterEach, Spec}
import fuds.Fuds
import io.shaka.http.Request.{PUT, GET}
import io.shaka.http.{TrustAllSslCertificates, Response}
import io.shaka.http.Status.{UNAUTHORIZED, OK}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.AUTHORIZATION
import scala.Some
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import sun.misc.BASE64Encoder
import FudsAuthorisationSpec.PimpedRequest

class FudsAuthorisationSpec extends Spec with BeforeAndAfterEach {
  TrustAllSslCertificates

  object `Server must` {
    def `--given an uploads white list-- only allow uploads when allowed by the white list`() {
      val fuds = Fuds.createFromBufferedSources(
        specifiedPort = None,
        contentWhiteList = None,
        uploadsWhiteList = Some(scala.io.Source.fromInputStream(new ByteArrayInputStream(
          "user:password".getBytes(StandardCharsets.UTF_8)
        ))),
        https = true
      )
      val base = s"https://localhost:${fuds.port}"

      try {
        val response: Response = http(
          PUT(base + "/fear.csv")
            .basicAuth("user", "password")
            .entity("foo,bar\n1,2\n")
        )
        assert((response.status, body(response)) ===(OK, Some("/fear.csv")))

        assert(http(
          PUT(base + "/fear.csv")
            .basicAuth("bob", "theHacker")
            .entity("foo,bar\n1,2\n")
        ).status === UNAUTHORIZED)

        // No Auth
        assert(http(
          PUT(base + "/fear.csv")
            .entity("foo,bar\n1,2\n")
        ).status === UNAUTHORIZED)
      }
      finally fuds.stop()
    }

    def `allow any uploads when there is no uploads authorisation white list`(){
      val fuds = Fuds.createFromBufferedSources(
        specifiedPort = None,
        contentWhiteList = None,
        uploadsWhiteList = None,
        https = true
      )

      val base = s"https://localhost:${fuds.port}"
      try {
        val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
        assert((response.status, body(response)) ===(OK, Some("/fear.csv")))
      }
      finally fuds.stop()
    }

    def `allow downloads regardless of the uploads white list`() {
      val fuds = Fuds.createFromBufferedSources(
        specifiedPort = None,
        contentWhiteList = None,
        uploadsWhiteList = Some(scala.io.Source.fromInputStream(new ByteArrayInputStream(
          "user:password".getBytes(StandardCharsets.UTF_8)
        ))),
        https = true
      )

      val base = s"https://localhost:${fuds.port}"
      try {
        okBody(http(PUT(base + "/files/fear.csv").basicAuth("user", "password").entity("foo,bar\n1,2\n")))

        val response = http(GET(base + "/files/fear.csv"))
        assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
      }
      finally fuds.stop()
    }
  }

  private def okBody(response: Response): String = {
    assert(response.status === OK)
    response.entityAsString.trim
  }
  private def body(response: Response): Option[String] = response.entity.map(_.toString().trim)
}

object FudsAuthorisationSpec {
  implicit class PimpedRequest(val req: io.shaka.http.Request) extends AnyVal {
    def basicAuth(user: String, password: String): io.shaka.http.Request = {
      req.header(AUTHORIZATION, "Basic " + new BASE64Encoder().encode(s"$user:$password".getBytes))
    }
  }
}