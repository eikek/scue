package org.eknet.scue.util

import com.tinkerpop.blueprints.{Edge, Vertex, Element}
import org.eknet.scue.{EdgeType, VertexType, ElementType}
import Operation.Operation

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 19:16
 */
abstract sealed class Event(e: Element, kind: ElementType, op: Operation) {
  def element = kind match {
    case VertexType => Left(e.asInstanceOf[Vertex])
    case EdgeType => Right(e.asInstanceOf[Edge])
  }
}

/**
 * Event indicating and add or removal of an vertex or edge.
 *
 * @param element
 * @param kind
 * @param op
 */
case class ElementEvent(element: Element, kind: ElementType, op: Operation)
case class PropertyEvent(element: Element, kind: ElementType, key: String, value: Option[Any], op: Operation)

object Operation extends Enumeration {
  type Operation = Value

  /**
   * This value indicates that either an element has been added or
   * a property has been set.
   *
   */
  val modify = Value

  /**
   * This value indicates that either an element or a property has
   * been deleted.
   */
  val delete = Value
}
