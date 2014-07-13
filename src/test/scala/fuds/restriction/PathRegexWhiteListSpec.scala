package fuds.restriction

import org.scalatest.Spec

class PathRegexWhiteListSpec extends Spec {
  object `PathRegexWhiteList must` {
    def `give the restriction when the path matches the regex`() {
      val restriction = new Restriction {
        override def apply(v1: Array[Byte]) = throw new RuntimeException("Shouldn't call this")
      }
      val slashAWhiteList = new PathRegexWhiteList("/A.*".r, restriction)
      assert(slashAWhiteList("/Anne") === restriction)
      assert(slashAWhiteList("/Bob") === Never)
    }
  }
}