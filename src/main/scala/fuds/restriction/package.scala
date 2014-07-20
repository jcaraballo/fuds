package fuds

package object restriction {
  type ContentRestriction = (Array[Byte] => Boolean)
  type ContentWhiteList = (String => ContentRestriction)

  type Credential = (String, String)
  type AuthorisationWhiteList = (Option[Credential] => Boolean)

  object NoContent extends ContentRestriction {
    override def apply(v1: Array[Byte]) = false
  }

  object AnyContent extends ContentRestriction {
    override def apply(v1: Array[Byte]): Boolean = true
  }

  object PermissiveContentWhiteList extends PathRegexContentWhiteList(".*".r, AnyContent)

  object PermissiveAuthorisationWhiteList extends AuthorisationWhiteList {
    override def apply(v1: Option[(String, String)]) = true
  }

  case class ExplicitAuthorisationWhiteList(allowed: List[Credential]) extends (Option[(String, String)] => Boolean) {
    override def apply(attemptedCredential: Option[Credential]) =
      attemptedCredential.exists(c => allowed.exists(_ == c))
  }

  object NoOne extends ExplicitAuthorisationWhiteList(Nil)
}