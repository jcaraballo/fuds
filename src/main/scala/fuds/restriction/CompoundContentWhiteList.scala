package fuds.restriction

object CompoundContentWhiteList {
  def apply(wls: List[ContentWhiteList]): ContentWhiteList = wls match {
    case Nil => throw new IllegalArgumentException("Empty white list")
    case head :: Nil => head
    case head :: tail =>
      new ContentWhiteList {
        override def apply(path: String): ContentRestriction =
          (ab: Array[Byte]) => head(path)(ab) || CompoundContentWhiteList(tail)(path)(ab)
      }
  }
}