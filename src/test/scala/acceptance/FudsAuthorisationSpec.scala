package acceptance

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import acceptance.FudsAuthorisationSpec.PimpedRequest
import acceptance.Helpers.{body, createdBody}
import fuds.{Fuds, Server}
import io.shaka.http.Http.http
import io.shaka.http.HttpHeader.AUTHORIZATION
import io.shaka.http.Request.{GET, PUT}
import io.shaka.http.Status.{CREATED, OK, UNAUTHORIZED}
import io.shaka.http.{Response, TrustAllSslCertificates}
import org.scalatest.{BeforeAndAfterEach, Spec}
import sun.misc.BASE64Encoder

class FudsAuthorisationSpec extends Spec with BeforeAndAfterEach {
  TrustAllSslCertificates

  object `Server must` {
    def `--given an uploads white list-- only allow uploads when allowed by the white list`() {
      val fuds = fudsForUploadWhiteList(Some("user:password"))
      val base = s"https://localhost:${fuds.port}"

      try {
        val response: Response = http(
          PUT(base + "/fear.csv")
            .basicAuth("user", "password")
            .entity("foo,bar\n1,2\n")
        )
        assert((response.status, body(response)) ===(CREATED, Some("/fear.csv")))

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
      val fuds = fudsForUploadWhiteList(None)

      val base = s"https://localhost:${fuds.port}"
      try {
        val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
        assert((response.status, body(response)) ===(CREATED, Some("/fear.csv")))
      }
      finally fuds.stop()
    }

    def `allow downloads regardless of the uploads white list`() {
      val fuds = fudsForUploadWhiteList(Some("user:password"))

      val base = s"https://localhost:${fuds.port}"
      try {
        createdBody(http(PUT(base + "/files/fear.csv").basicAuth("user", "password").entity("foo,bar\n1,2\n")))

        val response = http(GET(base + "/files/fear.csv"))
        assert((response.status, body(response)) === (OK, Some("foo,bar\n1,2")))
      }
      finally fuds.stop()
    }
  }

  private def fudsForUploadWhiteList(uploadsWhiteListFileContent: Option[String]): Server =
    Fuds.createFromBufferedSources(
      specifiedPort = None,
      contentWhiteList = None,
      uploadsWhiteList = uploadsWhiteListFileContent.map { c =>
        scala.io.Source.fromInputStream(new ByteArrayInputStream(
          c.getBytes(StandardCharsets.UTF_8)
        ))
      },
      keyStore = Some(("src/test/resources/certs/keystore-local.jks", "dummypass")),
      "target/files-" + java.util.UUID.randomUUID,
      shouldListDirectories = false
    )
}

object FudsAuthorisationSpec {
  implicit class PimpedRequest(val req: io.shaka.http.Request) extends AnyVal {
    def basicAuth(user: String, password: String): io.shaka.http.Request = {
      req.header(AUTHORIZATION, "Basic " + new BASE64Encoder().encode(s"$user:$password".getBytes))
    }
  }
}