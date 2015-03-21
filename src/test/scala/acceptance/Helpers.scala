package acceptance

import io.shaka.http.Response
import io.shaka.http.Status.{CREATED, OK}
import org.scalactic.TypeCheckedTripleEquals._

object Helpers {
  def body(response: Response): Option[String] = response.entity.map(_.toString().trim)

  def okBody(response: Response): String = {
    assert(response.status === OK)
    response.entityAsString.trim
  }

  def createdBody(response: Response): String = {
    assert(response.status === CREATED)
    response.entityAsString.trim
  }
}