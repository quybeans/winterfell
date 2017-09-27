
// copyright

package uniq.mqttchat.client

// scalastyle:off
import uniq.mqttchat.client.AdminClient._
import uniq.networking.mqtt.base.BaseClient
import uniq.networking.mqtt.base.model.MQTTCreateACLRuleRequest
import uniq.networking.mqtt.base.model.MQTTCreateUserRequest
import uniq.networking.mqtt.base.model.MQTTDeleteUserRequest
import scala.io.StdIn

import org.eclipse.paho.client.mqttv3.MqttTopic
import uniq.networking.mqtt.base.model.MQTTDeleteACLRuleRequest

final case class AdminClient(username: String, password: String) extends BaseClient(username, password) {
  override def login: Boolean = {
    super.login
  }


  override def printCommand: Unit = {
    println("1. Create an user")
    println("2. Delete an user")
    println("3. Add an access control rule")
    println("4. Delete an access control rule")
    println("0. exit")
    val cmd = StdIn.readLine("Please select your option: ")
    cmd match {
      case "1" => createUser
      case "2" => deleteUser
      case "3" => addACLRule
      case "4" => deleteACLRule
    }
  }

  private def addACLRule: Unit = {
    val username = StdIn.readLine("Username: ")
    val topic = StdIn.readLine("Topic: ")
    val canRead = StdIn.readLine("Can read: ").toBoolean
    val canWrite = StdIn.readLine("Can write: ").toBoolean
    if (!addACLRuleImp(username, topic, canRead, canWrite))
      println(s"Can not add ACL rule. The username $username does not exist.")
  }

  private def addACLRuleImp(
    username: String,
    topic: String,
    canRead: Boolean,
    canWrite: Boolean
  ): Boolean = {
    val data = MQTTCreateACLRuleRequest.encoder.apply(
      MQTTCreateACLRuleRequest(
        username,
        topic,
        canRead,
        canWrite
      )
    ).noSpaces
    val result = httpRequest(aclURL, "POST", data)
    204 == result.code
  }

  def deleteACLRule: Unit = {
    val username: String = StdIn.readLine("Username: ")
    val topic: String = StdIn.readLine("topic: ")
    val confirm = StdIn.readLine("Are you sure? ")
    if (confirm == "yes") {
      if (!deleteACLRuleImp(username, topic))
        println("username or topic name does not exist")
    }
  }

  def deleteACLRuleImp(username: String, topic: String): Boolean = {
    val data = MQTTDeleteACLRuleRequest.encoder.apply(
        MQTTDeleteACLRuleRequest(username,topic)
      )
      .noSpaces
      val result = httpRequest(aclURL, "DELETE", data)
      204 == result.code
  }

  private def deleteUser: Unit = {
    val username = StdIn.readLine("Username: ")
    val confirm = StdIn.readLine("Are you sure? ")
    if (confirm == "yes") {
      val data = MQTTDeleteUserRequest(username)
      val result = httpRequest(userURL.format(username), "DELETE")
      if (204 != result.code) {
        println(s"username $username does not exist")
      }
      else {
        deleteACLRuleImp(username, userTopic.format(username))
        deleteACLRuleImp("admin", userTopic.format(username))
      }
    }
  }

  private def createUser: Unit = {
    val username = StdIn.readLine("Username: ")
    val password = StdIn.readLine("Password: ")
    val data = MQTTCreateUserRequest.encoder.apply(
      MQTTCreateUserRequest(username, password)
    ).noSpaces
    val result = httpRequest(userURL.format(""), "POST", data)
    if (204 != result.code)
      println(s"username $username already exist.")
    else
      {
        addACLRuleImp(username, userTopic.format(username), true, true)
        addACLRuleImp("admin", userTopic.format(username), true, true)
      }
  }

}

object AdminClient {
  def apply(username: String, password: String): AdminClient = new AdminClient(username, password)
  private val userURL = "https://api.cloudmqtt.com/user/%s"
  private val aclURL = "https://api.cloudmqtt.com/acl"
  private val userTopic = "uniq/mqtt/chat/client/%s"
}