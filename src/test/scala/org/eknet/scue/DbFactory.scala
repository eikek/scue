package org.eknet.scue

import com.tinkerpop.blueprints.Graph
import java.io.File

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.11.12 22:00
 */
trait DbFactory[A <: Graph] {

  /**
   * Creates a new database with the given name, or opens the
   * database if it exists.
   *
   * @param name
   * @return
   */
  def createDb(name: String): NamedGraph[A]

  /**
   * Creates a new empty database.
   * @return
   */
  def newRandomDb: NamedGraph[A]

  /**
   * Destroys the given database.
   *
   * @param db
   */
  def destroy(db: NamedGraph[A])

}

object DbFactory {

  def newDir(path: String) = {
    val p = path.replace("/", File.separator)
    new File(p) match {
      case f if (f.exists() && f.isDirectory) => f
      case f if (!f.exists()) => f.mkdirs(); f
      case _ => sys.error("Unable to get or create dir: "+ p)
    }
  }

  def deleteAll(dir: File) {
    def removefiles(f: File) {
      if (f.isDirectory) {
        Option(f.listFiles()).map(_.foreach(removefiles))
      }
      f.delete()
    }
    removefiles(dir)
  }

  def withDb[A <: Graph](factory: DbFactory[A])(test: A => Any) {
    val db = factory.newRandomDb
    try {
      test(db)
    } finally {
      factory.destroy(db)
    }
  }
}
