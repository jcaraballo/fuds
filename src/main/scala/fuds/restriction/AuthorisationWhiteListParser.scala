package fuds.restriction

object AuthorisationWhiteListParser {
  def parse(lines: List[String]): AuthorisationWhiteList =
    ExplicitAuthorisationWhiteList(lines.map(_.trim).filterNot(_.isEmpty).map { line =>
      val split = line.split(":")
      if (split.size != 2) throw new IllegalArgumentException("Cannot parse user/password pair")

      (split(0), split(1))
    })
}