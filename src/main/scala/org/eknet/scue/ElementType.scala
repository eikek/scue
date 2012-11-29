package org.eknet.scue

import com.tinkerpop.blueprints.{Element, Edge, Vertex}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 14:37
 */
sealed trait ElementType {
  def name: String
  def elementClass: Class[_ <: Element]
}
object ElementType {
  def values = Set(VertexType, EdgeType)
  def from[A <: Element: Manifest] = {
    val clazz = manifest[A].erasure
    values.find(_.elementClass == clazz).get
  }
}
case object VertexType extends ElementType {
  val name = "vertex"
  val elementClass = classOf[Vertex]
}
case object EdgeType extends ElementType {
  val name = "edge"
  val elementClass = classOf[Edge]
}