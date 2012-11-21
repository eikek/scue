package org.eknet.scue

import com.tinkerpop.blueprints.Graph
import java.io.File
import java.util.UUID

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.11.12 22:05
 */
abstract class FileBasedDbFactory[A <: Graph](group:String, deleteOnExit:Boolean = true) extends DbFactory[A] {

  if (deleteOnExit) {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run() { DbFactory.deleteAll(baseDir) }
    }))
  }

  private[this] def baseDir = DbFactory.newDir("target/tests/"+group)

  protected def createDatabaseDir(name: String) = new File(baseDir, name)

  def open(dir: File): A

  def createDb(name: String) = {
    val graph = open(createDatabaseDir(name))
    new NamedGraph[A](name, graph)
  }

  def newRandomDb = createDb("testdb"+ UUID.randomUUID().toString)

  def destroy(db: NamedGraph[A]) {
    DbFactory.deleteAll(createDatabaseDir(db.name))
  }
}
