package fuds.restriction

import scala.util.matching.Regex


class PathRegexContentWhiteList(val TheRegex: Regex, val restriction: ContentRestriction) extends ContentWhiteList {
  override def apply(v1: String) = v1 match {
    case TheRegex() => restriction
    case _ => NoContent
  }
}