package org.eknet.scue

import com.tinkerpop.blueprints.TransactionalGraph
import java.util.concurrent.{ConcurrentMap, ConcurrentHashMap}
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion

/**
 * Introduces a simple tx concept as used by Neo4j
 * (see http://api.neo4j.org/current/org/neo4j/graphdb/Transaction.html)
 *
 * Use it like so
 * <pre>
 *   val tx = Transaction.start(graph)
 *   try {
 *     // graph code goes here
 *     tx.success()
 *   } finally {
 *     tx.finish()
 *   }
 * </pre>
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.10.12 18:58
 */
trait Transaction {

  def success()
  def finish()

}

object Transaction {
  private val canCommit = 1
  private val rollbackOnly = -1

  private val txmap = new ThreadLocal[ConcurrentMap[TransactionalGraph, java.lang.Integer]] {
    override def initialValue() = {
      new ConcurrentHashMap()
    }
  }

  def start(implicit db: TransactionalGraph): Transaction = {
    if (txmap.get().putIfAbsent(db, canCommit) == null)
      new TopTx(db)
    else
      new PlaceboTx(db)
  }

  private class TopTx(db: TransactionalGraph) extends Transaction {
    private var committed = false
    def success() { committed = true }

    def finish() {
      val successful = committed && txmap.get().remove(db) == canCommit
      if (successful) {
        db.stopTransaction(Conclusion.SUCCESS)
      } else {
        db.stopTransaction(Conclusion.FAILURE)
      }
    }
  }

  private class PlaceboTx(db: TransactionalGraph) extends Transaction {
    var committed = false
    def success() { committed = true }
    def finish() {
      if (!committed)
        txmap.get().put(db, rollbackOnly)
    }
  }
}