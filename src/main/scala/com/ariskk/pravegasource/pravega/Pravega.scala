package com.ariskk.pravegasource.pravega

import java.util.Collections

import io.pravega.connectors.flink.FlinkPravegaReader
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation

object Pravega {

  def source[T](config: PravegaConfig)(
    implicit event: KeyedEvent[T],
    v: Version[T],
    typeInfo: TypeInformation[T]
  ) = new FlinkPravegaReader[String](
    config.controller,
    config.scope,
    Collections.singleton(event.versionedStream),
    0L, // todo Start time. Manage offsets
    new SimpleStringSchema()
  )

}
