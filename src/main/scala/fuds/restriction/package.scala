package fuds

package object restriction {
  type ContentRestriction = (Array[Byte] => Boolean)
  type ContentWhiteList = (String => ContentRestriction)

  type AuthorisationWhiteList = (Option[(String, String)] => Boolean)

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
}