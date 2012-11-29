package org.eknet.scue

import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import java.util.UUID

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.11.12 17:30
 */
class TinkerDbFactory extends DbFactory[TinkerGraph] {

  def createDb(name: String) = new NamedGraph[TinkerGraph](name, new TinkerGraph())

  def newRandomDb = createDb(UUID.randomUUID().toString)

  def destroy(db: NamedGraph[TinkerGraph]) {
    db.db.clear()
    db.db.shutdown()
  }
}
