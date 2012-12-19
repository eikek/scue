package org.eknet.scue

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.ShouldMatchers
import java.util.concurrent.{TimeUnit, CountDownLatch, Callable, Executors}
import java.util.concurrent.atomic.AtomicInteger
import com.thinkaurelius.titan.core.TitanGraph

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.11.12 21:14
 */
class SimpleConcurrentTest extends DbFixture[TitanGraph] with ShouldMatchers with BeforeAndAfter {
  import GraphDsl._

  val factory = new TitanDbFactory()

  test ("concurrent write1") { implicit db =>
    val pool = Executors.newFixedThreadPool(3)
    val counter = new AtomicInteger(0)
    val startLatch = new CountDownLatch(1)
    val createVertex = new Callable[AnyRef] {
      def call() = {
        startLatch.await()
        withTx {
          val v1 = db.addVertex()
          val v2 = db.addVertex()
          db.addEdge(null, v1, v2, "testlabel"+counter.incrementAndGet())
          v2
        }
      }
    }

    val futures = List(createVertex, createVertex, createVertex) map (pool.submit(_))
    startLatch.countDown()
    val vertices = futures.map(_.get())
    withTx {
      vertices should have size (3)
      vertices(0) should not be (vertices(1))
      vertices(0) should not be (vertices(2))
      vertices(1) should not be (vertices(2))
    }
  }

  test ("concurrent write2") { implicit db =>
    val pool = Executors.newFixedThreadPool(3)
    val startLatch = new CountDownLatch(1)
    val counter = new AtomicInteger(0)
    val createVertex = new Callable[AnyRef] {
      def call() = {
        startLatch.await()
        withTx {
          val edgeLabel = "mylabel"+counter.incrementAndGet()
          val v = newVertex --> edgeLabel -->| newVertex
          v.getId
        }
      }
    }

    val futures = List(createVertex, createVertex, createVertex) map (pool.submit(_))
    startLatch.countDown()
    val vertices = futures.map(_.get())
    withTx {
      vertices should have size (3)
      vertices(0) should not be (vertices(1))
      vertices(0) should not be (vertices(2))
      vertices(1) should not be (vertices(2))
    }
  }

  test ("two threads with nested transactions") { implicit db =>
    import collection.JavaConversions._

    val pool = Executors.newFixedThreadPool(2)
    // Tx1 --TXs-createVertex----------------------------TXe--
    // Tx2 --------------------TXs--createVertex--TXe--------

    val peng1 = new CountDownLatch(1)
    val peng2 = new CountDownLatch(1)
    val peng3 = new CountDownLatch(1)

    val f0 = pool.submit(new Callable[Unit] {
      def call() {
        peng1.await(10, TimeUnit.SECONDS)
        withTx {
          newVertex --> "test1" --> newVertex
          peng2.countDown()
          peng3.await(10, TimeUnit.SECONDS)
        }
        withTx {
          db.getVertices.toList should have size (6)
          db.getEdges.toList should have size (3)
        }
      }
    })

    val f1 = pool.submit(new Callable[Unit] {
      def call() {
        peng2.await(10, TimeUnit.SECONDS)
        withTx {
          newVertex --> "test2" --> newVertex
          newVertex --> "test3" --> newVertex
        }
        withTx {
          db.getVertices.toList should have size (4)
          db.getEdges.toList should have size (2)
        }
      }
    })
    peng1.countDown()
    f0.get(20, TimeUnit.SECONDS)
    f1.get(20, TimeUnit.SECONDS)
    pool.shutdown()
  }
}
