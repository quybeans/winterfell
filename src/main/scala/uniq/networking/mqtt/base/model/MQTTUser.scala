// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.networking.mqtt.base.model

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.semiauto.deriveDecoder

final case class MQTTUser(
  username: String,
  password: String
)

object MQTTUser {
  implicit val encoder: Encoder[MQTTUser] = deriveEncoder
  implicit val decoder: Decoder[MQTTUser] = deriveDecoder
}
