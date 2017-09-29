// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.mqttchat.client

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder

final case class Message(
  username: String,
  content: String,
  runtime: Long
)

object Message {
  implicit val encoder: Encoder[Message] = deriveEncoder
  implicit val decoder: Decoder[Message] = deriveDecoder
}
