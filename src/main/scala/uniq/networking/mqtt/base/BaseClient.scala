
// copyright

package uniq.networking.mqtt.base

// scalastyle:off
import scalaj.http.Http
import scalaj.http.HttpResponse

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import uniq.networking.mqtt.base.BaseClient._
// scalastyle:on

class BaseClient(username: String, password: String){

  private[this] val settings = new MqttConnectOptions()

  private[this] def setAuth(username: String, password: String): Unit ={
    if (username.length > 0 && password.length > 0)
    {
      settings.setUserName(username)
      settings.setPassword(password.toArray)
    }
  }

  private[this] lazy val connector: MqttClient = {
    val client = new MqttClient(brokerURI, username)
    client
  }

  def login: Boolean = {
    setAuth(username, password)
    connector.connect(settings)
    connector.isConnected
  }

  def disconnect: Unit = {
    connector.disconnect()
  }

  protected def httpRequest(
    url: String,
    method: String,
    jsonData: String = ""
  ): HttpResponse[String]  = {
    Http(url).auth(cloudMqttUser, cloudMqttPass)
      .header("Content-Type", "application/json")
      .postData(jsonData)
      .method(method)
      .asString
  }

  def printCommand: Unit = {
    println("disconnect")
    disconnect
  }

}

object BaseClient{
  private val brokerURI = "ssl://m10.cloudmqtt.com:28374"
  private val chanelTopic = "uniq/mqtt/chat/chanel/"
  private val cloudMqttUser = "piybrutp"
  private val cloudMqttPass = "04hHfiCooyL_"
  private val alcRulesUrl = "https://api.cloudmqtt.com/acl"
}