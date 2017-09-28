
// copyright

package uniq.networking.mqtt.base.model

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.semiauto.deriveDecoder

final case class MQTTACLRule(
  username: String,
  topic: String,
  read: Boolean,
  write: Boolean,
)

object MQTTACLRule {
  implicit val encoder: Encoder[MQTTACLRule] = deriveEncoder[MQTTACLRule]
  implicit val decoder: Decoder[MQTTACLRule] = deriveDecoder[MQTTACLRule]
}
