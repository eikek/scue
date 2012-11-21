package org.eknet.scue

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints.{Edge, Vertex}
import java.util.concurrent.{Callable, Executors, TimeUnit, CountDownLatch}
import sun.util.logging.resources.logging
import com.thinkaurelius.titan.core.TitanGraph

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.10.12 18:07
 * 
 */
class CreationSuite extends DbFixture[TitanGraph] with ShouldMatchers with BeforeAndAfter {

  val factory = new TitanDbFactory()

  import GraphDsl._

  test ("create outgoing edge") { implicit db =>
    var id0, id1: AnyRef = null
    var e: Edge = null
    withTx {
      val v0 = newVertex //create vertex
      val v1 = newVertex //create vertex
      e = v0 --> "test" --> v1 //create edge
      id0 = v0.getId
      id1 = v1.getId
    }
    withTx {
      val v0 = db.getVertex(id0)
      val v1 = db.getVertex(id1)
      (v0 ->- "test").headOption should be (Some(e))
      (v0 ->- "test") should have size (1)
    }
  }

  test ("create incoming edge") { implicit db =>
    var id0, id1: AnyRef = null
    var e: Edge = null
    withTx {
      val v0 = newVertex
      val v1 = newVertex
      e = v0 <-- "test" <-- v1
      id0 = v0.getId
      id1 = v1.getId
    }
    withTx {
      val v0 = db.getVertex(id0)
      val edges = v0 -<- "test"
      edges should have size 1
      edges.head should be (e)
    }
  }

  test ("create single vertex") { implicit db =>
    var v0, v1: AnyRef = null
    withTx { v0 = vertex("name" := "vertexus").getId }
    withTx { v1 = vertex("name" := "vertexus").getId }

    withTx {
      v0 should be (v1)

      vertices should have size (1)
      vertices.head.getId should be (v0)
      vertices.head.getId should be (v1)
    }
  }

  test ("create vertex with properties") { implicit db =>
    withTx {
      vertex("name" := "ref") --> "mylabel" --> newVertex("name" := "test", "redirect" := true)
    }
    withTx {
      val v = vertices("name" := "test").head
      v("redirect") should be (Some(true))
    }
  }

  test ("create vertex with properties from map") { implicit db =>
    val props = Map(
      "name" -> "soldout",
      "redirect" -> true,
      "age" -> 23
    )

    withTx {
      vertex("name" := "ref") --> "mylabel" --> newVertex(props: _*)
    }
    withTx {
      val v = vertices("name" := "soldout").head
      v.has("redirect" := true) should be (true)
      v.has("age" := 23) should be (true)
      v("redirect") should be (Some(true))
      v("age") should be (Some(23))
    }
  }

}
