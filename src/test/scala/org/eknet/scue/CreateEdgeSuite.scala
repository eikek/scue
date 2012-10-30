package org.eknet.scue

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.10.12 18:07
 * 
 */
class CreateEdgeSuite extends OrientDbFactory with FunSuite with ShouldMatchers with BeforeAndAfter {

  import GraphDsl._

  test ("create connection") {
    implicit val db = createDb
    val v0 = newVertex //create vertex
    val v1 = newVertex //create vertex
    val e = v0 --> "test" --> v1 //create edge
    db.stopTransaction(Conclusion.SUCCESS)

    (v0 ->- "test").headOption should be (Some(e))
    (v0 ->- "test") should have size (1)
  }

}
