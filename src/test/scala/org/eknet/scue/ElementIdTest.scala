package org.eknet.scue

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import com.tinkerpop.blueprints.{Vertex, Element}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.12.12 23:10
 */
class ElementIdTest extends DbFixture[OrientGraph] with ShouldMatchers {

  val factory = new OrientDbFactory()

  import GraphDsl._

  test ("load elements") { implicit db =>
    val id = withTx {
      val v = vertex("name" := "test")
      ElementId(v)
    }

    withTx {
      val x = findVertex(id.id)
      val z = findElement(id)
      x should be (z)
    }

    val xid: ElementId[Element] = id
    withTx {
      val el = findElement(xid)
      el should be (findVertex(id.id))
    }
  }

  test ("create element ids") { implicit db =>
    withTx {
      val v = newVertex
      val eid = ElementId(v)
      eid.kind should be (VertexType)
    }

    withTx {
      val e = newVertex --> "anedge" --> newVertex
      val eid = ElementId(e)
    }
  }

}
