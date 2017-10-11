// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.networking.mqtt.base.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class MQTTUserACLRule(
  topic: String,
  read: Boolean,
  write: Boolean
)

object MQTTUserACLRule {
  implicit val decoder: Decoder[MQTTUserACLRule] = deriveDecoder
}
