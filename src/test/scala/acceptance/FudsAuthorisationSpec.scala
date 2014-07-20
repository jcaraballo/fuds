package acceptance

import org.scalatest.{BeforeAndAfterEach, Spec}
import fuds.Fuds
import io.shaka.http.Request.{PUT,GET}
import io.shaka.http.{TrustAllSslCertificates, Response}
import io.shaka.http.Status.{UNAUTHORIZED, OK}
import io.shaka.http.Http.http
import fuds.restriction._
import scala.Some

class FudsAuthorisationSpec extends Spec with BeforeAndAfterEach {
  TrustAllSslCertificates

  object `Server must` {
    def `allow uploads when allowed by the uploads authorisation white list`() {
      val fuds = Fuds.startHttps(PermissiveAuthorisationWhiteList)
      val base = s"https://localhost:${fuds.port}"

      try {
        val response: Response = http(PUT(base + "/fear.csv").entity("foo,bar\n1,2\n"))
        assert((response.status, body(response)) ===(OK, Some("/fear.csv")))
      }
      finally fuds.stop()
    }

    def `not allow uploads when not allowed by the uploads authorisation white list`(){
      val intolerant: AuthorisationWhiteList = new AuthorisationWhiteList {
        override def apply(v1: Option[(String, String)]) = false
      }
      val fuds = Fuds.startHttps(uploadsWhiteList = intolerant)
      try {
        val response: Response = http(PUT(s"https://localhost:${fuds.port}/fear.csv").entity("foo,bar\n1,2\n"))
        assert(response.status === UNAUTHORIZED)
      }
      finally fuds.stop()
    }
  }

  private def body(response: Response): Option[String] = response.entity.map(_.toString().trim)
}