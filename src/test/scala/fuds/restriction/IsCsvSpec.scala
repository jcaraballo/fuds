package fuds.restriction

import org.scalatest.Spec
import java.nio.charset.StandardCharsets.UTF_8

class IsCsvSpec extends Spec {
    object `IsCsv must` {
      def `accept empty files`() {
        assert(IsCsv(bytes("")) === true)
      }

      def `accept files with only one line`(){
        assert(IsCsv(bytes("foo")) === true)
        assert(IsCsv(bytes("foo, bar")) === true)
      }

    def `reject files with lines with more elements than the first one`(){
      assert(IsCsv(bytes("\nfoo")) === false)
      assert(IsCsv(bytes("foo\none, two")) === false)
    }

    def `reject files with quotes (as we are not using a proper csv parser)`(){
      assert(IsCsv(bytes(""""foo","bar"""")) === false)
    }
  }

  object `numberOfElements must` {
    def `be 0 for empty lines`() {
      assert(IsCsv.numberOfElements("") === 0)
    }

    def `be the number of commas plus one (must count blanks at the sides of the commas as fields)`() {
      assert(IsCsv.numberOfElements("one") === 1)
      assert(IsCsv.numberOfElements("one,two") === 2)
      assert(IsCsv.numberOfElements("one,") === 2)
      assert(IsCsv.numberOfElements(",two") === 2)
      assert(IsCsv.numberOfElements(",") === 2)
    }

    def `count spaces as a field character`() {
      assert(IsCsv.numberOfElements(" ") === 1)
      assert(IsCsv.numberOfElements(" , ") === 2)
      assert(IsCsv.numberOfElements(" ,") === 2)
      assert(IsCsv.numberOfElements(", ") === 2)
    }
  }

  private def bytes(s: String): Array[Byte] = s.getBytes(UTF_8)
}