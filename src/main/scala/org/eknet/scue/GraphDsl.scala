/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.scue

import scala.Tuple2
import com.tinkerpop.blueprints.{Direction, TransactionalGraph, KeyIndexableGraph, Graph, Element, Edge, Vertex}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.10.12 19:26
 */
trait GraphDsl {
  import collection.JavaConversions._

  implicit def toVertex(v: RichVertex): Vertex = v.v
  implicit def toRichVertex(v: Vertex): RichVertex = new RichVertex(v)

  implicit def toRichEdge(e: Edge): RichEdge= new RichEdge(e)
  implicit def toEdge(e: RichEdge): Edge = e.e

  implicit def toRichElement(e: Element) = new RichElement(e)
  implicit def toElement(re: RichElement) = re.el

  implicit def any2PropertyAssoc(x: String): PropertyAssoc = new PropertyAssoc(x)

  /**
   * Looks up a vertex using the specified id.
   *
   * @param id
   * @param graph
   * @return
   */
  def findVertex(id: AnyRef)(implicit graph: Graph) = Option(graph.getVertex(id))

  /**
   * Looks up an edge using the specified id.
   *
   * @param id
   * @param graph
   * @return
   */
  def findEdge(id: AnyRef)(implicit graph: Graph) = Option(graph.getEdge(id))

  /**
   * Looksup either an edge or a vertex using the specified id.
   * @param id
   * @param graph
   * @return
   */
  def findElement[A <: Element](id: ElementId[A])(implicit graph: Graph): Option[A] = id.kind match {
    case VertexType => findVertex(id.id).asInstanceOf[Option[A]]
    case EdgeType => findEdge(id.id).asInstanceOf[Option[A]]
  }

  /**
   * Creates a new vertex and adds it to the graph.
   * @param graph
   * @return
   */
  def newVertex(id: AnyRef, init: Vertex => Unit)(implicit graph: Graph): Vertex = {
    val v = graph.addVertex(id)
    init(v)
    v
  }
  def newVertex(id: AnyRef)(implicit graph: Graph): Vertex = newVertex(id, v => ())

  def newVertex(init: Vertex => Unit)(implicit graph: Graph): Vertex = newVertex(null, init)

  def newVertex(implicit graph: Graph): Vertex = newVertex(null, v => ())

  /**
   * Creates a new vertex with the given key-value property
   * if it does not already exists. The vertex is indexed by
   * the given key.
   *
   * @param p the key-value property that is indexed and used to lookup existing vertex
   * @param init optional function to further initialize the new vertex.
   *             this is only used if the vertex does not already
   *             exists and is therefore created.
   * @return
   */
  def vertex(p: Property, init: Vertex => Unit = v => ())(implicit graph: KeyIndexableGraph): Vertex = {
    import collection.JavaConversions._

    val (name, value) = p
    graph.getIndexedKeys(VertexType.elementClass).find(_ == name) getOrElse {
      graph.createKeyIndex(name, VertexType.elementClass)
    }

    graph.getVertices(name, value).find(v => v.has(p)) getOrElse {
      val v = newVertex(init)
      v(name) = value
      v
    }
  }

  private[this] def getSingle[A](iter: Iterator[A]) = {
    if (!iter.hasNext) {
      None
    } else {
      iter.next() match {
        case x if (!iter.hasNext) => Some(x)
        case _ => throw new IllegalStateException("There are more than one element.")
      }
    }
  }

  def singleVertex(p: Property)(implicit db: Graph) = getSingle(vertices(p).iterator)

  def vertices(p: Property)(implicit db: Graph) = db.getVertices(p._1, p._2).toIterable

  def vertices(implicit db: Graph) = db.getVertices.toIterable

  def singleEdge(p: Property)(implicit db: Graph) = getSingle(edges(p).iterator)

  def edges(p: Property)(implicit db: Graph) =
    db.getEdges(p._1, p._2).toIterable

  def edges(implicit db: Graph) = db.getEdges.toIterable

  def withTx[A](f: => A)(implicit db: TransactionalGraph) = {
    val tx = Transaction.start
    try {
      val x = f
      tx.success()
      x
    } finally {
      tx.finish()
    }
  }
}

object GraphDsl extends GraphDsl

final class EdgeOut(v: Vertex, label: String) {

  /**
   * Creates a new edge to the vertex `o` and adds it to the graph.
   * @param o
   * @param db
   * @return
   */
  def -->(o: Vertex)(implicit db: Graph) = db.addEdge(null, v, o, label)

  /**
   * Same as `-->` but returns the other vertex
   * @param o
   * @param db
   * @return
   */
  def -->|(o: Vertex)(implicit db: Graph) = { db.addEdge(null, v, o, label); o }

}

final class EdgeIn(v: Vertex, label: String) {
  /**
   * Creates a new edge from the given vertex `o` and adds it to the graph
   * @param o
   * @param db
   * @return
   */
  def <--(o: Vertex)(implicit db: Graph) = db.addEdge(null, o, v, label)

  /**
   * Same as `<---` but returns the other vertex.
   *
   * @param o
   * @param db
   * @return
   */
  def <--|(o: Vertex)(implicit db: Graph) = { db.addEdge(null, o, v, label); o }

}
final class DynEdge(v: Vertex, dir: Direction, label: String) {

  /**
   * Creates a new edge with direction `dir` and adds
   * it to the graph.
   *
   * @param o
   * @param db
   * @return
   */
  def ---(o: Vertex)(implicit db: Graph) = {
    if (dir == Direction.OUT)
      new RichEdge(db.addEdge(null, v, o, label))
    else
      new RichEdge(db.addEdge(null, o, v, label))
  }
}

class RichVertex(val v: Vertex) extends RichElement(v) {

  import collection.JavaConverters._

  /**
   * Start creating a new outgoing edge.
   * @param label
   * @return
   */
  def -->(label: String): EdgeOut = new EdgeOut(v, label)

  /**
   * Start creating a new edge with the given direction.
   *
   * @param dir
   * @param label
   * @return
   */
  def ---(dir: Direction, label: String) = new DynEdge(v, dir, label)

  /**
   * Start creating a new incoming edge.
   * @param label
   * @return
   */
  def <--(label: String): EdgeIn = new EdgeIn(v, label)


  /**
   * Iterate over all outgoing edges with the given labels.
   * @param labels
   * @return
   */
  def ->-(labels: String*) = new EdgeIterable(v, Direction.OUT, labels)

  /**
   * Iterate over all incoming edges with the given labels.
   * @param labels
   * @return
   */
  def -<-(labels: String*) =  new EdgeIterable(v, Direction.IN, labels)

  /**
   * Iterate over all edges with the given labels.
   *
   * @param labels
   * @return
   */
  def -<>-(labels: String*) = new EdgeIterable(v, Direction.BOTH, labels)

  /**
   * Iterate over all adjecent vertices.
   *
   * @param labels
   * @return
   */
  def adjacents(labels: String*) =
    v.getVertices(Direction.BOTH, labels: _*).asScala

  /**
   * Iterate over all edges.
   *
   * @param labels
   * @return
   */
  def edges(labels: String*) =
    v.getEdges(Direction.BOTH, labels: _*).asScala
}

class RichEdge(val e: Edge) extends RichElement(e) {
  def label = e.getLabel
  def inVertex = e.getVertex(Direction.IN)
  def outVertex = e.getVertex(Direction.OUT)
  def other(v: Vertex) = v match {
    case x if (x == inVertex) => outVertex
    case x if (x == outVertex) => inVertex
    case _ => sys.error("Given vertex is not part of this edge: "+ v)
  }
}
class RichElement(val el: Element) {
  import collection.JavaConverters._

  /**
   * Gets the value for the given key and casts it to the specified type.
   * @param key
   * @tparam A
   * @return
   */
  def get[A](key:String) = Option(el.getProperty(key)).map(_.asInstanceOf[A])

  /**
   * Gets the value for the given key.
   * @param key
   * @return
   */
  def apply(key: String) = Option(el.getProperty(key))

  /**
   * Sets the given property for this element.
   * @param p
   * @return
   */
  def update(p: Property): this.type = { el.setProperty(p._1, p._2); this }

  /**
   * Sets all given properties for this element.
   * @param t
   * @return
   */
  def +=(t: Property*): this.type = { t.foreach({ case (name, value) => update(name, value) }); this }

  /**
   * Sets all properties given in the map for this element.
   *
   * @param map
   * @return
   */
  def +=(map: Map[String, Any]): this.type = this.+=(map.toSeq: _*)

  /**
   * Removes all given properties.
   *
   * @param keys
   * @return
   */
  def -=(keys: String*): this.type = { keys.foreach(k => el.removeProperty(k)); this }

  /**
   * Returns whether this element has all given properties set. All property values
   * must equal.
   * @param t
   * @return
   */
  def has(t: Property*) = t.foldLeft(true)((b, t) => b && el.getProperty(t._1) == t._2)

  /**
   * Returns all property keys for this element.
   *
   * @return
   */
  def keySet = el.getPropertyKeys.asScala
}

class EdgeIterable(v: Vertex, dir: Direction, labels: Seq[String]) extends Iterable[Edge] {
  import collection.JavaConverters._

  def iterator = v.getEdges(dir, labels: _*).asScala.iterator

  /**
   * Iterate over adjencent vertices.
   * @return
   */
  def ends = v.getVertices(dir, labels: _*).asScala

  //shortcuts
  def findEnd(p: Vertex => Boolean) = ends find p
  def filterEnds(p: Vertex => Boolean) = ends filter p
  def foreachEnd[A](f: Vertex => A) { ends foreach f }
  def mapEnds[A](f: Vertex => A) = ends map f

}

final class PropertyAssoc(val x: String) {
  @inline def := [B](y: B): (String, B) = Tuple2(x, y)
}
