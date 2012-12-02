package org.eknet.scue

import com.tinkerpop.blueprints.{Element, Edge, Vertex}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 14:37
 */
sealed trait ElementType {
  def elementClass: Class[_ <: Element]
  def id(id: AnyRef): ElementId[Element]
}
object ElementType {
  def values = Set(VertexType, EdgeType)
  def from[A <: Element: Manifest] = {
    val clazz = manifest[A].erasure
    values.find(_.elementClass == clazz).getOrElse(sys.error("No type for: "+ clazz))
  }
}
case object VertexType extends ElementType {
  val elementClass = classOf[Vertex]
  def id(id: AnyRef) = ElementId[Vertex](id)
}
case object EdgeType extends ElementType {
  val elementClass = classOf[Edge]
  def id(id: AnyRef) = ElementId[Edge](id)
}