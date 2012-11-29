package org.eknet.scue.util

import com.tinkerpop.blueprints.{Element, KeyIndexableGraph, Vertex, Edge, Graph}
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.11.12 23:40
 */
abstract class ForwardingGraph[T <: Graph] extends Graph with Proxy {
  override def self: T
  def getFeatures = self.getFeatures
  def addVertex(id: Any) = self.addVertex(id)
  def getVertex(id: Any) = self.getVertex(id)
  def removeVertex(vertex: Vertex) { self.removeVertex(vertex) }
  def getVertices = self.getVertices
  def getVertices(key: String, value: Any) = self.getVertices(key, value)
  def addEdge(id: Any, outVertex: Vertex, inVertex: Vertex, label: String) = self.addEdge(id, outVertex, inVertex, label)
  def getEdge(id: Any) = self.getEdge(id)
  def removeEdge(edge: Edge) { self.removeEdge(edge) }
  def getEdges = self.getEdges
  def getEdges(key: String, value: Any) = self.getEdges(key, value)
  def shutdown() { self.shutdown() }
}