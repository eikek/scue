package org.eknet.scue

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints.{Edge, Vertex}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.10.12 18:07
 * 
 */
class CreationSuite extends FunSuite with ShouldMatchers with BeforeAndAfter {

  import GraphDsl._

  after { OrientDbFactory.deleteAll() }

  test ("create outgoing edge") {
    implicit val db = OrientDbFactory.createDb
    var v0, v1: Vertex = null
    var e: Edge = null
    withTx {
      v0 = newVertex //create vertex
      v1 = newVertex //create vertex
      e = v0 --> "test" --> v1 //create edge
    }
    (v0 ->- "test").headOption should be (Some(e))
    (v0 ->- "test") should have size (1)
  }

  test ("create incoming edge") {
    implicit val db = OrientDbFactory.createDb
    var v0, v1: Vertex = null
    var e: Edge = null
    withTx {
      v0 = newVertex
      v1 = newVertex
      e = v0 <-- "test" <-- v1
    }
    val edges = v0 -<- "test"
    edges should have size 1
    edges.head should be (e)
  }

  test ("create single vertex") {
    implicit val db = OrientDbFactory.createDb
    var v0, v1: Vertex = null
    withTx { v0 = vertex("name", "vertexus") }
    withTx { v1 = vertex("name", "vertexus") }

    v0 should be (v1)

    vertices should have size (1)
    vertices.head should be (v0)
    vertices.head should be (v1)
  }
}