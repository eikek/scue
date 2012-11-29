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
 * @param kind
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 14:36
 */
case class ElementId(id: AnyRef, kind: ElementType) {
  def this(e: Edge) = this(e.getId, EdgeType)
  def this(v: Vertex) = this(v.getId, VertexType)
  def this(e: Element, kind: ElementType) = this(e.getId, kind)
}
