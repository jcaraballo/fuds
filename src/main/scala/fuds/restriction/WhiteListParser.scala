package fuds.restriction


object WhiteListParser {
  def parse(lines: List[String]): WhiteList = {
    val whiteLists = lines.map(_.trim).filterNot(_.isEmpty).map { line =>
      val positionOfFirstSpace = line.indexOf(' ')
      val restriction = RestrictionParser.parse(line.substring(0, positionOfFirstSpace))
      val regex = line.substring(positionOfFirstSpace + 1).r
      new PathRegexWhiteList(regex, restriction)
    }
    if(whiteLists.isEmpty) throw new IllegalArgumentException("Empty white list")
    else CompoundWhiteList(whiteLists)
  }
}

object RestrictionParser {
  def parse(restrictionAsString: String): Restriction = restrictionAsString match {
    case "IsCsv" => IsCsv
    case "AnyContent" => AnyContent
    case _ => throw new IllegalArgumentException(s"Unrecognised restriction '$restrictionAsString'")
  }
}