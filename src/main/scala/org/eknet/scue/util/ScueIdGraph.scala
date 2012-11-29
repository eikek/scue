package org.eknet.scue.util

import com.tinkerpop.blueprints.{Edge, Vertex, Element, Index, TransactionalGraph, Graph, IndexableGraph, KeyIndexableGraph}
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph
import org.eknet.scue.{ElementId, EdgeType, VertexType, GraphDsl, ElementType}
import java.util.UUID

/**
 * Another wrapper that adds ids to each node and vertex using an `ElementListener`.
 *
 * The underlying graph may either be a [[com.tinkerpop.blueprints.KeyIndexableGraph]] or an
 * [[com.tinkerpop.blueprints.IndexableGraph]]. The former is always prefered in cases the
 * underlying graph implements both interfaces.
 *
 *@author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 00:13
 */
class ScueIdGraph(g: Either[KeyIndexableGraph, IndexableGraph], idFactory: () => String = ScueIdGraph.defaultIdFactory)
  extends ForwardingGraph[Graph] with TransactionalGraph with GraphDsl {

  private implicit val db = this
  import ScueIdGraph.idProperty

  private val idIndexName = "org.eknet.scue.$ScueIdIndex$"

  override val self = ScueIdGraph.createDelegate(g)

  val indexableGraph = {
    self.getBaseGraph match {
      case g: KeyIndexableGraph => None
      case g: IndexableGraph => Some(g)
      case _ => sys.error("Unreachable code")
    }
  }
  val keyIndexGraph = {
    self.getBaseGraph match {
      case g: KeyIndexableGraph => Some(g)
      case _ => None
    }
  }

  indexableGraph.map(g => {
    ElementType.values.foreach { kind =>
      if (g.getIndex(idIndexName, kind.elementClass) == null)
         g.createIndex(idIndexName, kind.elementClass)
    }
  })
  keyIndexGraph.map(g => {
    ElementType.values.foreach { kind =>
      if (!g.getIndexedKeys(kind.elementClass).contains(idProperty))
        g.createKeyIndex(idProperty, kind.elementClass)
    }
  })

  private[this] def addIdProperty(kind: ElementType, element: Element): String = {
    element.get[String](idProperty) getOrElse {
      val id = idFactory()
      element(idProperty) = id
      indexableGraph.map { g =>
        g.getIndex(idIndexName, kind.elementClass).asInstanceOf[Index[Element]].put(idProperty, id, element)
      }
      id
    }
  }

  private[this] def removeIdProperty(kind: ElementType, element: Element) {
    indexableGraph map { g =>
      element(idProperty) map { id =>
        g.getIndex(idIndexName, kind.elementClass).asInstanceOf[Index[Element]].remove(idProperty, id, element)
      }
    }
  }

  self.addElementListener(e => {
    e.op match {
      case Operation.modify => addIdProperty(e.kind, e.element)
      case Operation.delete => removeIdProperty(e.kind, e.element)
    }
  })

  /**
   * Iterates through all vertices and edges and adds a unique id property.
   *
   */
  def initializeGraph() {
    withTx {
      vertices.foreach(addIdProperty(VertexType, _))
      edges.foreach(addIdProperty(EdgeType, _))
    }
  }

  override val getFeatures = {
    val f = super.getFeatures.copyFeatures()
    f.ignoresSuppliedIds = Boolean.box(false)
    f.isWrapper = Boolean.box(true)
    f
  }

  override def getEdge(id: Any) = {
    Option(super.getEdge(id)) getOrElse {
      edges(idProperty := id.toString).headOption.orNull
    }
  }

  override def getVertex(id: Any) = {
    Option(super.getVertex(id)) getOrElse {
      vertices(idProperty := id.toString).headOption.orNull
    }
  }


  override def addVertex(id: Any) = {
    Option(id).flatMap(id => Option(getVertex(id)))
      .map(x => throw new IllegalArgumentException("Vertex with id '"+id+"' already existst"))

    val v = super.addVertex(id)
    Option(id).map(id => v(idProperty) = id.toString)

    v
  }

  override def addEdge(id: Any, outVertex: Vertex, inVertex: Vertex, label: String) = {
    Option(id).flatMap(id => Option(getEdge(id)))
      .map(x => throw new IllegalArgumentException("Edge with id '"+id+"' already existst"))

    val e = super.addEdge(id, outVertex, inVertex, label)
    Option(id).map(id => e(idProperty) = id.toString)

    e
  }

  def stopTransaction(conclusion: Conclusion) {
    self.getBaseGraph match {
      case txg: TransactionalGraph => txg.stopTransaction(conclusion)
      case _ =>
    }
  }
}

object ScueIdGraph {

  //some db impls don't like points in key property names.
  private[util] val idProperty = "org_eknet_scue_scueElementId"

  private val defaultIdFactory = () => UUID.randomUUID().toString

  private def createDelegate(graph: Either[KeyIndexableGraph, IndexableGraph]) = {
    graph.fold(identity, identity) match {
      case g: EventGraph[_] => new EventGraph[Graph](g.getBaseGraph.asInstanceOf[Graph]) with ScueEventGraph
      case g => new EventGraph[Graph](g) with ScueEventGraph
    }
  }

  def getId(element: Element): Option[String] = Option(element.getProperty(idProperty).asInstanceOf[String])

  def getElementId(v: Vertex): Option[ElementId] = getElementId(v, VertexType)
  def getElementId(e: Edge): Option[ElementId] = getElementId(e, EdgeType)
  def getElementId(e: Element, kind: ElementType): Option[ElementId] = getId(e).map(id => ElementId(id, kind))

  def wrap(g: KeyIndexableGraph, initialize: Boolean): ScueIdGraph = {
    val graph = new ScueIdGraph(Left(g))
    if (initialize) graph.initializeGraph()
    graph
  }

  def wrap(g: IndexableGraph, initialize: Boolean): ScueIdGraph = {
    val graph = new ScueIdGraph(Right(g))
    if (initialize) graph.initializeGraph()
    graph
  }

  def wrap(graph: Graph, initialize: Boolean): ScueIdGraph = graph match {
    case g: ScueIdGraph => g
    case g: KeyIndexableGraph => wrap(g, initialize)
    case g: IndexableGraph => wrap(g, initialize)
    case _ => sys.error("Graph '"+graph+"' not compatible for adding id properties")
  }
}