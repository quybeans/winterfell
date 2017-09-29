// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.networking.mqtt.base

// scalastyle:off underscore.import
import scalaj.http.Http
import scalaj.http.HttpResponse

import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import uniq.mqttchat.client.Message
import uniq.networking.mqtt.base.BaseClient._
// scalastyle:on underscore.import

class BaseClient(username: String, password: String) {

  private[this] val settings = new MqttConnectOptions()

  private[this] def setAuth(username: String, password: String): Unit = {
    if (username.length > 0 && password.length > 0) {
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
    if (jsonData.nonEmpty) {
      Http(url).auth(cloudMqttUser, cloudMqttPass)
        .header("Content-Type", "application/json")
        .postData(jsonData)
        .method(method)
        .asString
    }
    else {
      Http(url).auth(cloudMqttUser, cloudMqttPass)
        .method(method)
        .asString
    }
  }

  def subscribe(topic: String): Unit = {
    println(s"subscribe to $topic")
    connector.subscribe(topic)
  }

  def unsubscribe(topic: String): Unit = {
    println(s"unsubscribe to $topic")
    connector.unsubscribe(topic)
  }

  def publish(topic: String, msg: Message): Unit = {
    connector.publish(
      topic,
      new MqttMessage(
        Message.encoder.apply(msg).noSpaces.getBytes()
      )
    )
  }

  def setMqttCallback(callback: MqttCallback): Unit = {
    connector.setCallback(callback)
  }

  def isConnected: Boolean = connector.isConnected

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
  val userURL = "https://api.cloudmqtt.com/user/%s"
  val aclURL = "https://api.cloudmqtt.com/acl"
  val userTopic = "uniq/mqtt/chat/client/%s"
  val channelTopic = "uniq/mqtt/chat/chanel/%s"
  val GET = "GET"
  val DELETE = "DELETE"
  val POST = "POST"
  val areYouSure = "Are you sure [Y/N]? "
  val Y = "Y"
  val strChannel = "channel: "
  val strUsername = "username: "
  val HTTPNoContentCode = 204
}
