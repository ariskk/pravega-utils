package com.ariskk.pravegasource.utils

import cats.syntax.either._
import io.circe.{parser, Decoder}

object JsonUtils {

  def decode[T: Decoder](t: String) =
    for {
      parsed <- parser.parse(t)
      decoded <- Decoder[T].apply(parsed.hcursor)
    } yield decoded

}
