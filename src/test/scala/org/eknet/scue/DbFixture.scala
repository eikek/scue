package org.eknet.scue

import com.tinkerpop.blueprints.Graph
import org.scalatest.fixture.FunSuite

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.11.12 21:36
 */
trait DbFixture[A <: Graph] extends FunSuite {

  def factory: DbFactory[A]

  type FixtureParam = A

  def withFixture(test: OneArgTest) {
    DbFactory.withDb(factory)(db => withFixture(test.toNoArgTest(db)))
  }

}
