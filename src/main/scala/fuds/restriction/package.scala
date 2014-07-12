package fuds

package object restriction {
  type WhiteList = (String => Restriction)
  type Restriction = (Array[Byte] => Boolean)

  object Never extends Restriction {
    override def apply(v1: Array[Byte]) = false
  }
}