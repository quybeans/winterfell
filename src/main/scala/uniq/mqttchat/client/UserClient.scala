// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.mqttchat.client

import scala.io.StdIn

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import uniq.networking.mqtt.base.BaseClient
import uniq.networking.mqtt.base.model.MQTTUserACLRules

// scalastyle:off underscore.import
import uniq.networking.mqtt.base.BaseClient._
// scalastyle:on underscore.import

class UserClient(username: String, password: String) extends BaseClient(username, password) {

  private[this] lazy val mqttCallback = new MqttCallback {

    override def deliveryComplete(token: IMqttDeliveryToken): Unit = {
    }

    override def connectionLost(cause: Throwable): Unit = {
      println("connection lost")
      println("Press any key to exit")
      StdIn.readLine()
      System.exit(1)
    }

    override def messageArrived(topic: String, json: MqttMessage): Unit = {
      val message = io.circe.parser.decode[Message](json.toString).toTry.get
      if (message.username != username) {
        println(s"${message.username}: ${message.content}")
      }
    }
  }

  private[this] lazy val myTopic = userTopic.format(username)

  override def login: Boolean = {
    super.login
    setMqttCallback(mqttCallback)
    subscribe(myTopic)
    this.isConnected
  }

  override def printCommand: Unit = {
    println("1. join chanel")
    println("0. exit")
    val cmd = StdIn.readLine("Please select your option: ")
    cmd match {
      case "1" => joinChanel
      case "0" =>
        this.disconnect
        System.exit(0)
    }
    printCommand
  }

  private def joinChanel: Unit = {
    listAllChanel
    val channel = StdIn.readLine("please select a chanel: ")
    subscribe(channelTopic.format(channel))
    doChat(channel)
  }

  private def doChat(channel: String): Unit = {
    val msg = StdIn.readLine(s"$username: ")
    if (msg != "exit"){
      if (msg.nonEmpty) {
        publish(
          channelTopic.format(channel),
          Message(username, msg, System.currentTimeMillis() / 1000)
        )
      }
      doChat(channel)
    }
    else {
      unsubscribe(channelTopic.format(channel))
    }
  }

  private def listAllChanel: Unit = {
    val result = httpRequest(userURL.format(username), GET)
    val userRules = io.circe.parser.decode[MQTTUserACLRules](result.body).toTry.get
    println(s"all chanel of $username")
    printf("%-30s\t%-5s\t%-5s\n", "chanel", "read", "write")
    userRules.acls.foreach(
      acl => {
        val index = acl.topic.indexOf(channelTopic.format(""))
        if (index > -1) {
          printf(
            "%-30s\t%-5s\t%-5s\n",
            acl.topic.replace(channelTopic.format(""), ""),
            acl.read,
            acl.write
          )
        }
      }
    )
  }
}

object UserClient{
  def apply(username: String, password: String): UserClient = new UserClient(username, password)
}
