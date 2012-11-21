package org.eknet.scue

import java.util.concurrent.atomic.AtomicInteger
import java.io.File
import com.thinkaurelius.titan.core.{TitanGraph, TitanFactory}
import java.util.UUID

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.11.12 21:01
 */
class TitanDbFactory(deleteOnExit: Boolean = true) extends FileBasedDbFactory[TitanGraph]("titandbs", deleteOnExit) {

  def open(dir: File) = {
    TitanFactory.open(dir.getAbsolutePath)
  }

}
