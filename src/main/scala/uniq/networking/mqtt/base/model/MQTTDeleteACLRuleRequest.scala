
// copyright

package uniq.networking.mqtt.base.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class MQTTDeleteACLRuleRequest(
  username: String,
  topic: String
)

object MQTTDeleteACLRuleRequest {
  implicit val encoder: Encoder[MQTTDeleteACLRuleRequest] = deriveEncoder[MQTTDeleteACLRuleRequest]
}
