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
class OrientDbFactory(deleteOnExit:Boolean = true) extends FileBasedDbFactory[OrientGraph]("orientdbs", deleteOnExit) {

  def open(dir: File) = {
    val url = "local://"+ dir.getAbsolutePath
    new OrientGraph(url)
  }
}
