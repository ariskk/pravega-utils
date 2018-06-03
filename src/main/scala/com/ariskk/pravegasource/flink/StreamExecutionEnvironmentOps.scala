package com.ariskk.pravegasource.flink

import cats.syntax.either._
import com.ariskk.pravegasource.pravega.{KeyedEvent, Pravega, PravegaConfig, Version}
import com.ariskk.pravegasource.utils.JsonUtils
import io.circe.Decoder
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala._

abstract class StreamExecutionEnvironmentOps(
  @transient val e: StreamExecutionEnvironment
) extends java.io.Serializable {

  def addPravegaSource[Event](
    pravegaConfig: PravegaConfig
  )(
    implicit v: Version[Event],
    event: KeyedEvent[Event],
    mf: Decoder[Event],
    typeInfo: TypeInformation[Event]
  ): DataStream[Event] =
    e.addSource(Pravega.source[Event](pravegaConfig))
      .name(s"${event.versionedStream}-pravega-source")
      .uid(s"${event.versionedStream}-pravega-source")
      .startNewChain()
      .flatMap(JsonUtils.decode[Event](_).toOption) // At your own risk

}
