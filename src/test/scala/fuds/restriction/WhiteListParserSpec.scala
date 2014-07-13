package fuds.restriction

import org.scalatest.Spec
import java.nio.charset.StandardCharsets._

class WhiteListParserSpec extends Spec {
  object `WhiteListParser must` {
    def `understand <restriction> <regex> as paths matching the regex must submit to the restriction`(){
      val whiteList = WhiteListParser.parse(List("IsCsv a.*"))
      assert(whiteList("almond").getClass === IsCsv.getClass)
      assert(whiteList("banana").getClass === Never.getClass)
    }

    def `ignore spaces on the sides`(){
      val actual = WhiteListParser.parse(List("  IsCsv a.*  ")).asInstanceOf[PathRegexWhiteList]
      assert((actual.TheRegex.toString(), actual.restriction) === ("a.*", IsCsv))
    }

    def `rejects empty configuration files`(){
      intercept[IllegalArgumentException]{WhiteListParser.parse(List(""))}
      intercept[IllegalArgumentException]{WhiteListParser.parse(List("   "))}
      intercept[IllegalArgumentException]{WhiteListParser.parse(List("   ", ""))}
    }

    def `ignore blank lines`(){
      val whiteList = WhiteListParser.parse(List("", "IsCsv a.*", ""))
      assert(whiteList("almond").getClass === IsCsv.getClass)
    }

    def `compound multiple lines as multiple white lists`(){
      val whiteList = WhiteListParser.parse(List("IsCsv a.*", "IsCsv b.*"))

      // Seems to match CSVs
      assert(whiteList("almond")(bytes("foo,bar\none,two")) === true)
      assert(whiteList("almond")(bytes("foo\none,two")) === false)

      // Seems to match CSVs
      assert(whiteList("banana")(bytes("foo,bar\none,two")) === true)
      assert(whiteList("banana")(bytes("foo\none,two")) === false)

      // Does not seems to match CSVs
      assert(whiteList("chutney")(bytes("foo,bar\none,two")) === false)
      assert(whiteList("chutney")(bytes("foo\none,two")) === false)
    }
  }

  private def bytes(s: String): Array[Byte] = s.getBytes(UTF_8)
}