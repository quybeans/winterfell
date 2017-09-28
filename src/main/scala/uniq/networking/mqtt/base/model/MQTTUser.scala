
// copyright

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
  implicit val encoder: Encoder[MQTTUser] = deriveEncoder[MQTTUser]
  implicit val decoder: Decoder[MQTTUser] = deriveDecoder[MQTTUser]
}