# scue

**sc**ala bl**ue**prints is a tiny scala library that provides
a dsl for working with the [blueprints graph api](http://blueprints.tinkerpop.com).

## Usage

Add the library to your project, for example with [maven](http://maven.apache.org):

    <dependency>
      <groupId>org.eknet.scue</groupId>
      <artifactId>scue_2.9.2</artifactId>
      <version>0.2.0-SNAPSHOT</version>
    </dependency>

or with [sbt](http://www.scala-sbt.org):

    "org.eknet.scue" %% "scue" % "0.2.0-SNAPSHOT"

You can either mix in the trait `GraphDsl` or import the members of its companion
object. Next, put a `Graph` object in scope and annotate it with the `implicit` keyword.

By default, all code that accesses graph elements should be wrapped in a transaction.

    import GraphDsl._
    implicit db: TransactionalGraph = ...
    withTx {
       ...
    }

### Create and Modify Elements

Create a new vertex

    newVertex

Shortcuts for accessing properties:

    val v: Vertex = ...
    v("key") = "value"
    v("key")  //returns Some("value") of type Option[AnyRef]
    v.get[String]("key")  //casts Some("value") to Option[String]
    v += ("key2" := "value2", "key3" := "value3") //adds all properties
    v -= "key2" //removes property with "key2"
    val map = Map("season" -> "winter", "temp_celcius" -> 8)
    v += map

Create a new vertex and run some initializing code

    newVertex( v => v("name") = "winter" )

Create a new vertex with a specific property. The vertex is only created, if no vertex
exists with this property. In this case the found vertex is returned. A key-index is
created for the given key (this only works with `KeyIndexableGraph`).

    vertex("key" := "value")
    vertex("key" -> "value")

The `:=` creates a tuple, same way as `->`.

Add initializer function that is executed if the vertex is newly created

    vertex("key" := "value", v => v("id") = "myid")

Creating edges:

    newVertex --> "alabel" --> newVertex //returns the edge
    vertex("name" := "winter") --> "is-before" -->| newVertex //returns "newVertex"

    newVertex <-- "alabal" <-- newVertex
    vertex("name" := "winter") <-- "is-after" <-- vertex("name" := "spring")


### Traversing

Find all vertices/edges with a given property

    vertices("name" := "winter")
    edges("name" := "winter")

To find a single vertex/edge (returns `None` if not found and throws if there are more than one),

    singleVertex("name" := "winter")
    singleEdge("name" := "winter")

Traverse adjacent edges

    v ->- "alabel" //outgoing edges, return type: Iterable[Edge]
    v ->- "label" ends //outgoing edges, return type: Iterable[Vertex]
    v -<- "alabel" //incoming edges
    v -<>- "alabel" //both edges

Specify more labels or none:

    v ->- ("label1", "label2)

There are shortcuts for filtering and mapping adjacent vertices

    v ->- "label" mapEnds(v => ...)
    v ->- "label" filterEnds(v => ...)
