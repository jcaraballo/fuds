package fuds.restriction

import org.scalatest.Spec


class AuthorisationWhiteListParserSpec extends Spec {
  object `AuthorisationWhiteListParser must` {
    def `not authorise anyone when there is an empty authorisation white list`() {
      assert(AuthorisationWhiteListParser.parse(List("")) === NoOne)
    }

    def `only authorise the listed pairs of user:password`() {
      val authorisationWhiteList = AuthorisationWhiteListParser.parse(List("user1:password1", "user2:password2"))
      assert(authorisationWhiteList(Some("user1", "password1")) === true)
      assert(authorisationWhiteList(Some("user2", "password2")) === true)
      assert(authorisationWhiteList(Some("hacker", "l1t3")) === false)
    }

    def `reject (blow up) authorisation white list lines that are not <user>:<password>`(){
      intercept[IllegalArgumentException]{
        AuthorisationWhiteListParser.parse(List("user1:password1", "foobar", "user2:password2"))
      }
    }
  }
}