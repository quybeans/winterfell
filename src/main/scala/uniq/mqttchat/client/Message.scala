
// copyright

package uniq.mqttchat.client
// scalastyle:off
import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
// scalastyle:on

final case class Message(
  username: String,
  content: String,
  runtime: Long
)

object Message {
  implicit val encoder: Encoder[Message] = deriveEncoder
  implicit val decoder: Decoder[Message] = deriveDecoder
}