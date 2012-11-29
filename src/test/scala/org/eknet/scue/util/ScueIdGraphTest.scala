package org.eknet.scue.util

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.scue.{OrientDbFactory, TinkerDbFactory, TitanDbFactory, DbFixture}
import com.thinkaurelius.titan.core.TitanGraph
import com.tinkerpop.blueprints.impls.tg.{TinkerGraphFactory, TinkerGraph}
import com.tinkerpop.blueprints.{Graph, KeyIndexableGraph}
import org.eknet.neoswing.utils.QuickView
import com.tinkerpop.blueprints.impls.orient.OrientGraph

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.11.12 23:37
 */
class ScueIdGraphTest extends DbFixture[OrientGraph] with ShouldMatchers {

  val factory = new OrientDbFactory()

  test ("create elements with id") { db =>
    import org.eknet.scue.GraphDsl._
    implicit val idg = ScueIdGraph.wrap(TinkerGraphFactory.createTinkerGraph().asInstanceOf[KeyIndexableGraph], true)

    vertices should not be ('empty)
    vertices.foreach(v => ScueIdGraph.getId(v) should not be (None))

    val x = newVertex --> "test" -->| newVertex
    ScueIdGraph.getId(x) should not be (None)
    x -<>- () foreach(e => ScueIdGraph.getId(e) should not be (None))
    x -<>- () foreachEnd(e => ScueIdGraph.getId(e) should not be (None))

  }

  test ("lookup elements with artificial id") { db =>
    import org.eknet.scue.GraphDsl._
    implicit val idb = ScueIdGraph.wrap(db.asInstanceOf[Graph], false)

    val v0 = withTx(newVertex("name" := "eike"))
    val id0 = v0.getId
    val id1 = ScueIdGraph.getId(v0).get

    id0 should not be id1
    findVertex(id0).get should be (v0)
    findVertex(id1).get should be (v0)
  }

  test ("use custom ids") { db =>

    import org.eknet.scue.GraphDsl._
    implicit val idb = ScueIdGraph.wrap(db.asInstanceOf[Graph], false)

    val v0 = idb.addVertex("myvertex")
    val id0 = v0.getId
    val id1 = ScueIdGraph.getId(v0).get
    id1 should  be ("myvertex")

    id0 should not be id1
    findVertex(id0).get should be (v0)
    findVertex(id1).get should be (v0)

    intercept[IllegalArgumentException] {
      idb.addVertex("myvertex")
    }
  }
}
