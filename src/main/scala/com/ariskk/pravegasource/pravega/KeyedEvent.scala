package com.ariskk.pravegasource.pravega

trait KeyedEvent[T] {
  def key(t: T): KeyedEvent.Key
  def stream: KeyedEvent.StreamName
  def versionedStream(implicit v: Version[T]) = s"$stream-${v.value}"
}

object KeyedEvent {
  type Key = String
  type StreamName = String
  def apply[T: KeyedEvent]: KeyedEvent[T] = implicitly
}

trait Version[T] {
  def value: Long
}

trait LowPriorityVersion {
  implicit def version[T] = new Version[T] {
    override def value: Long = 1L
  }
}

object Version extends LowPriorityVersion {
  def apply[T: Version]: Version[T] = implicitly
}