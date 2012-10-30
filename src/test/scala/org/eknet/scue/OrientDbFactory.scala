package org.eknet.scue

import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import com.tinkerpop.blueprints.impls.orient.OrientGraph

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.10.12 18:07
 * 
 */
abstract class OrientDbFactory {

  def createDb(name: String) = new OrientGraph(OrientDbFactory.dbUrl(name))
  def createDb = new OrientGraph(OrientDbFactory.nextDb)
  def deleteAll() { OrientDbFactory.deleteAll() }
}

object OrientDbFactory {
  private val counter = new AtomicInteger(0)

  val databaseDir = newDir("target/tests/orientdbs")

  private def nextDb = dbUrl("testdb"+counter.getAndIncrement)
  private def dbUrl(name: String) = "local://"+ new File(databaseDir, name).getAbsolutePath

  private def newDir(path: String) = {
    val p = path.replace("/", File.separator)
    new File(p) match {
      case f if (f.exists() && f.isDirectory) => f
      case f if (!f.exists()) => f.mkdirs(); f
      case _ => sys.error("Unable to get or create dir: "+ p)
    }
  }
  private def deleteAll() {
    def removefiles(f: File) {
      if (f.isDirectory) {
        Option(f.listFiles()).map(_.foreach(removefiles))
      }
      f.delete()
    }
    removefiles(OrientDbFactory.databaseDir)
  }

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run() { deleteAll() }
  }))
}
