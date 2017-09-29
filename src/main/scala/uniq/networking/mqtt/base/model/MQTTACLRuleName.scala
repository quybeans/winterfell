// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.networking.mqtt.base.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class MQTTACLRuleName(
  username: String,
  topic: String
)

object MQTTACLRuleName {
  implicit val encoder: Encoder[MQTTACLRuleName] = deriveEncoder
}
