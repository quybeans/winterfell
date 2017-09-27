
// copyright

package uniq.networking.mqtt.base.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class MQTTCreateACLRuleRequest(
  username: String,
  topic: String,
  read: Boolean,
  write: Boolean,
)

object MQTTCreateACLRuleRequest {
  implicit val encoder: Encoder[MQTTCreateACLRuleRequest] = deriveEncoder[MQTTCreateACLRuleRequest]
}
