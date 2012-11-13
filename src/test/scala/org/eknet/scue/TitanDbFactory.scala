package org.eknet.scue

import java.util.concurrent.atomic.AtomicInteger
import java.io.File
import com.thinkaurelius.titan.core.TitanFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.11.12 21:01
 */
object TitanDbFactory {
  private val counter = new AtomicInteger(0)

  val databaseDir = newDir("target/tests/titandbs")

  def createDb(name: String) = TitanFactory.open(OrientDbFactory.dbUrl(name))
  def createDb = TitanFactory.open(OrientDbFactory.nextDb)

  def nextDb = dbUrl("testdb"+counter.getAndIncrement)
  def dbUrl(name: String) = new File(databaseDir, name).getAbsolutePath

  private def newDir(path: String) = {
    val p = path.replace("/", File.separator)
    new File(p) match {
      case f if (f.exists() && f.isDirectory) => f
      case f if (!f.exists()) => f.mkdirs(); f
      case _ => sys.error("Unable to get or create dir: "+ p)
    }
  }

  def deleteAll() {
    def removefiles(f: File) {
      if (f.isDirectory) {
        Option(f.listFiles()).map(_.foreach(removefiles))
      }
      f.delete()
    }
    removefiles(TitanDbFactory.databaseDir)
  }

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run() { deleteAll() }
  }))
}
