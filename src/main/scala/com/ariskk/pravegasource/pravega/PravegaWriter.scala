package com.ariskk.pravegasource.pravega

import scala.compat.java8.FutureConverters._

import io.circe.Encoder
import io.circe.generic.semiauto._
import cats.syntax.traverse._
import cats.instances.list._
import cats.instances.either._
import cats.syntax.either._
import io.pravega.client.ClientFactory
import io.pravega.client.admin.StreamManager
import io.pravega.client.stream.impl.JavaSerializer
import io.pravega.client.stream.{EventStreamWriter, EventWriterConfig, ScalingPolicy, StreamConfiguration}
import monix.eval.Task
import shapeless._
import ops.hlist._
import ops.coproduct.ToHList

class PravegaWriter[ForEvents <: Coproduct] private (writers: Map[KeyedEvent.StreamName, EventStreamWriter[String]]) {

  // The writers get generated using the `ForEvents` coproduct so there is no way one is not there
  // todo use a coproduct of tagged EventStreamWriter to make this look prettier
  private def getWriterForType[T](implicit k: KeyedEvent[T]): EventStreamWriter[String] =
    writers.get(k.stream).getOrElse(throw new Exception("This cannot happen"))

  def writeEvent[T: Encoder](t: T)(
    implicit v: Version[T],
    k: KeyedEvent[T],
    selector: ops.coproduct.Selector[ForEvents, T]
  ): Task[Unit] =
    Task
      .fromFuture(
        getWriterForType[T]
          .writeEvent(
            KeyedEvent[T].key(t),
            Encoder[T].apply(t).spaces2
          )
          .toScala
      )
      .map(_ => ())

}

object PravegaWriter {

  private def buildWriter(config: PravegaConfig, stream: KeyedEvent.StreamName) =
    ClientFactory
      .withScope(config.scope, config.controller)
      .createEventWriter(stream, new JavaSerializer[String], EventWriterConfig.builder().build())

  object StreamNames extends Poly1 {
    implicit def forEvent[T] = at[KeyedEvent[T]] { x =>
      x.stream
    }
  }

  case class PravegaWriterBuilder[Events <: Coproduct](discard: None.type) extends AnyVal {
    def apply[
      EventsList <: HList,
      KeyedEvents <: HList,
      KeyedEventsMapped <: HList
    ](config: PravegaConfig)(
      implicit list: ToHList.Aux[Events, EventsList],
      keyedEventsLifted: ops.hlist.LiftAll.Aux[KeyedEvent, EventsList, KeyedEvents],
      mapper: ops.hlist.Mapper.Aux[StreamNames.type, KeyedEvents, KeyedEventsMapped],
      toList: ops.hlist.ToTraversable.Aux[KeyedEventsMapped, List, String]
    ) = {

      val allStreams = keyedEventsLifted.instances.map(StreamNames).toList

      lazy val streamManager = StreamManager.create(config.controller)
      lazy val streamConfig = StreamConfiguration
        .builder()
        .scalingPolicy(ScalingPolicy.fixed(1))
        .build()

      for {
        _ <- Either.catchNonFatal(streamManager.createScope(config.scope))
        _ <- allStreams.traverse(s => Either.catchNonFatal(streamManager.createStream(config.scope, s, streamConfig)))
        writers = allStreams.map(s => s -> buildWriter(config, s)).toMap
        pravegaWriter = new PravegaWriter[Events](writers)
      } yield pravegaWriter

    }
  }

  def newInstance[Events <: Coproduct] = PravegaWriterBuilder[Events](None)

}
