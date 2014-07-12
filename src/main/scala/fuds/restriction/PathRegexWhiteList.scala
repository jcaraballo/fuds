package fuds.restriction

import scala.util.matching.Regex


class PathRegexWhiteList(val TheRegex: Regex, val restriction: Restriction) extends WhiteList {
  override def apply(v1: String) = v1 match {
    case TheRegex() => restriction
    case _ => Never
  }
}