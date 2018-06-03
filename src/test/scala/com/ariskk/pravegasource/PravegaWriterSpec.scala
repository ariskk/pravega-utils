package com.ariskk.pravegasource

import java.net.URI

import io.circe.generic.semiauto._
import cats.syntax.either._
import shapeless._
import shapeless.test._
import com.ariskk.pravegasource.pravega._
import com.ariskk.pravegasource.pravega.KeyedEvent.{Key, StreamName}
import org.scalatest.{FunSpec, Matchers}

class PravegaWriterSpec extends FunSpec with Matchers {

  case class Foo(id: String, name: String)
  object Foo {
    implicit val k = new KeyedEvent[Foo] {
      override def key(t: Foo): Key = t.id
      override def stream: StreamName = "foos"
    }
    implicit val encoder = deriveEncoder[Foo]
  }

  case class Bar(id: String, name: String, otherData: List[String])
  object Bar {
    implicit val k = new KeyedEvent[Bar] {
      override def key(t: Bar): Key = t.id
      override def stream: StreamName = "bars"
    }
    implicit val encoder = deriveEncoder[Bar]
  }

  case class Baz(id: String, no: String)
  object Baz {
    implicit val k = new KeyedEvent[Baz] {
      override def key(t: Baz): Key = t.id
      override def stream: StreamName = "bars"
    }
    implicit val encoder = deriveEncoder[Baz]
  }

  case class Other(id: String, field: Long)

  lazy val config = PravegaConfig(URI.create("tcp://127.0.0.1:9090"), "test-scope")

  describe("PravegaWriter should") {

    it("only work for the types it was constructed for") {
      type AllPravegaEvents = Foo :+: Bar :+: CNil

      lazy val writer = PravegaWriter
        .newInstance[AllPravegaEvents](config)
        .fold[PravegaWriter[AllPravegaEvents]](t => throw t, x => x)

      val baz = Baz("ab", "abc")

      illTyped("""writer.write(baz)""")
    }

    it("shouldn't create an instance if a type is missing type class instances") {
      type AllPravegaEvents = Foo :+: Other :+: CNil

      illTyped(
        """
          |PravegaWriter
          |  .newInstance[AllPravegaEvents](config)
          |  .fold[PravegaWriter[AllPravegaEvents]](t => throw t, x => x)
          |"""
      )

    }
  }

}
