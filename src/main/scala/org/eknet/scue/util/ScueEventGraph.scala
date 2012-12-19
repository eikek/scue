package org.eknet.scue.util

import com.tinkerpop.blueprints.util.wrappers.event.EventGraph
import com.tinkerpop.blueprints.{Edge, Vertex, Graph}
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener
import org.eknet.scue.{EdgeType, VertexType}

/**
 * A trait that can be mixed in with [[com.tinkerpop.blueprints.util.wrappers.event.EventGraph]]
 * to use more scala friendly methods for adding listeners.
 *
 * To wrap an existing [[com.tinkerpop.blueprints.util.wrappers.event.EventGraph]], just recreate
 * it
 * {{{
 *   val eg: EvenGraph[T]
 *   val newEg = new EventGraph[T](eg.getBaseGraph) with ScueEventGraph
 * }}}
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.11.12 23:21
 */
trait ScueEventGraph {
  this: EventGraph[_ <: Graph] =>

  type ElementListener = ElementEvent => Any
  type PropertyListener = PropertyEvent => Any

  def addElementListener(l: ElementListener) {
    this.addListener(new ElementListenerWrapper(l))
  }

  def addPropertyListener(l: PropertyListener) {
    this.addListener(new PropertyListenerWrapper(l))
  }
}

/**
 * Allows chaining `ElementListener` and `PropertyListener`.
 *
 * This class will apply the given listener and return the argument
 * allowing the next listener to execute.
 *
 * @param f the listener function to wrap.
 * @tparam A
 */
case class Chain[A](f: A => Any) extends (A => A) {
  def apply(v1: A) = { f(v1); v1 }
  def andThen(g: A => Any) = { Chain(super.andThen(g)) }
}

abstract class GraphChangedAdapter extends GraphChangedListener {
  def vertexAdded(vertex: Vertex) {}
  def vertexPropertyChanged(vertex: Vertex, key: String, setValue: Any) {}
  def vertexPropertyRemoved(vertex: Vertex, key: String, removedValue: Any) {}
  def vertexRemoved(vertex: Vertex) {}
  def edgeAdded(edge: Edge) {}
  def edgePropertyChanged(edge: Edge, key: String, setValue: Any) {}
  def edgePropertyRemoved(edge: Edge, key: String, removedValue: Any) {}
  def edgeRemoved(edge: Edge) {}
}

private[util] class ElementListenerWrapper(l: ScueEventGraph#ElementListener) extends GraphChangedAdapter {
  override def vertexAdded(vertex: Vertex) {
    l(new ElementEvent(vertex, VertexType, Operation.modify))
  }

  override def vertexRemoved(vertex: Vertex) {
    l(new ElementEvent(vertex, VertexType, Operation.delete))
  }

  override def edgeAdded(edge: Edge) {
    l(new ElementEvent(edge, EdgeType, Operation.modify))
  }

  override def edgeRemoved(edge: Edge) {
    l(new ElementEvent(edge, EdgeType, Operation.delete))
  }
}

private[util] class PropertyListenerWrapper(l: ScueEventGraph#PropertyListener) extends GraphChangedAdapter {
  override def vertexPropertyChanged(vertex: Vertex, key: String, setValue: Any) {
    l(new PropertyEvent(vertex, VertexType, key, Option(setValue), Operation.modify))
  }

  override def vertexPropertyRemoved(vertex: Vertex, key: String, removedValue: Any) {
    l(new PropertyEvent(vertex, VertexType, key, Option(removedValue), Operation.delete))
  }

  override def edgePropertyChanged(edge: Edge, key: String, setValue: Any) {
    l(new PropertyEvent(edge, EdgeType, key, Option(setValue), Operation.modify))
  }

  override def edgePropertyRemoved(edge: Edge, key: String, removedValue: Any) {
    l(new PropertyEvent(edge, EdgeType, key, Option(removedValue), Operation.delete))
  }
}