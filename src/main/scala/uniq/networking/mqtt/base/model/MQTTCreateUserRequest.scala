
// copyright

package uniq.networking.mqtt.base.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class MQTTCreateUserRequest(
  username: String,
  password: String
)

object MQTTCreateUserRequest {
  implicit val encoder: Encoder[MQTTCreateUserRequest] = deriveEncoder[MQTTCreateUserRequest]
}