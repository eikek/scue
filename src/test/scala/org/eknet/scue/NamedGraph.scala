package org.eknet.scue

import com.tinkerpop.blueprints.Graph

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.11.12 22:09
 */
case class NamedGraph[+A <: Graph](name: String, db: A)
object NamedGraph {
  implicit def toGraphDb[A <: Graph](ng: NamedGraph[A]) = ng.db
}
