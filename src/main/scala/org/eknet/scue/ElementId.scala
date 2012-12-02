package org.eknet.scue

import com.tinkerpop.blueprints.{Element, Edge, Vertex}

/**
 * A simple class that represents an id for either an edge or a vertex. The
 * [[com.tinkerpop.blueprints.Vertex]] and [[com.tinkerpop.blueprints.Edge]]
 * are not always allowed to exists between transactions, this class can be
 * used to hold references to vertices and edges outside the scope of an
 * transaction.
 *
 * @param id
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 14:36
 */
case class ElementId[+A <: Element :Manifest](id: AnyRef) {
  val kind = ElementType.from[A]
}

object ElementId {
  def apply(v: Vertex): ElementId[Vertex] = ElementId[Vertex](v.getId)
  def apply(e: Edge): ElementId[Edge] = ElementId[Edge](e.getId)
}
