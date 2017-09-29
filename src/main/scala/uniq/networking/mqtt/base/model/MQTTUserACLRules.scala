// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.networking.mqtt.base.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class MQTTUserACLRules(
  username: String,
  password: String,
  acls: List[MQTTUserACLRule]
)

object MQTTUserACLRules {
  implicit val decoder: Decoder[MQTTUserACLRules] = deriveDecoder
}
