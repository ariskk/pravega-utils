package com.ariskk.pravegasource.flink

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

trait FlinkExtensions {

  implicit class PimpedStreamExecutionEnvironment(override val e: StreamExecutionEnvironment)
      extends StreamExecutionEnvironmentOps(e)

}

object FlinkExtensions extends FlinkExtensions
