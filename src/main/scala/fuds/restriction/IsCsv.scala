package fuds.restriction

import java.nio.charset.StandardCharsets._


object IsCsv extends ContentRestriction {
  override def apply(v1: Array[Byte]) = {
    val content = new String(v1, UTF_8)
    val lines = content.split("\n")
    lines.size>0 && !lines.exists(_.contains('"')) && {
      val headerSize = numberOfElements(lines(0))
      lines.tail.forall(numberOfElements(_)<=headerSize)
    }
  }
  def numberOfElements(line: String): Int = if(line=="") 0 else line.count(_ == ',') + 1
}