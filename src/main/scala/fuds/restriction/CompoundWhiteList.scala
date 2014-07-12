package fuds.restriction

object CompoundWhiteList {
  def apply(wls: Seq[WhiteList]): WhiteList = wls match {
    case head :: Nil => head
    case head :: tail =>
      new WhiteList {
        override def apply(path: String): Restriction =
          (ab: Array[Byte]) => head(path)(ab) || CompoundWhiteList(tail)(path)(ab)
      }
  }
}