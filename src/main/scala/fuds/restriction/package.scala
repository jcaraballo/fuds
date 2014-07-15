package fuds

package object restriction {
  type Restriction = (Array[Byte] => Boolean)
  type WhiteList = (String => Restriction)

  object Never extends Restriction {
    override def apply(v1: Array[Byte]) = false
  }

  object AnyContent extends Restriction {
    override def apply(v1: Array[Byte]): Boolean = true
  }

  object PermissiveWhiteList extends PathRegexWhiteList(".*".r, AnyContent)
}