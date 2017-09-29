// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.mqttchat.client

import scala.io.StdIn

import io.circe.parser.decode
import uniq.networking.mqtt.base.BaseClient
import uniq.networking.mqtt.base.model.MQTTACLRule
import uniq.networking.mqtt.base.model.MQTTACLRuleName
import uniq.networking.mqtt.base.model.MQTTUser
import uniq.networking.mqtt.base.model.MQTTUserACLRules

// scalastyle:off underscore.import
import uniq.networking.mqtt.base.BaseClient._
import uniq.mqttchat.client.AdminClient._
// scalastyle:on underscore.import

final case class AdminClient(
  username: String,
  password: String) extends BaseClient(username, password) {
  override def login: Boolean = {
    super.login
  }

  // scalastyle:off cyclomatic.complexity
  override def printCommand: Unit = {
    println("1. Create an user")
    println("2. Delete an user")
    println("3. List all users")
    println("4. Add an access control rule")
    println("5. Delete an access control rule")
    println("6. List all access control rules")
    println("7. List all access control rules by username")
    println("8. List all chat channel")
    println("9. Create a chat channel")
    println("10. Delete a chat channel")
    println("11. Ban user from a chat channel")
    println("0. exit")
    val cmd = StdIn.readLine("Please select your option: ")
    cmd match {
      case "1" => createUser
      case "2" => deleteUser
      case "3" => listAllUsers
      case "4" => addACLRule
      case "5" => deleteACLRule
      case "6" => listAllACLRules
      case "7" => listACLRulesByUsername
      case "8" => listAllChatChannel
      case "9" => createChatChannel
      case "10" => deleteChatChannel
      case "11" => banUserFromChatChannel
      case _ =>
        this.disconnect
        System.exit(0)
    }
    println("Press any key to continue...")
    StdIn.readLine()
    printCommand
  }
  // scalastyle:on cyclomatic.complexity

  private def listAllUsers: Unit = {
    decode[List[MQTTUser]](httpRequest(userURL.format(""), GET).body) match {
      case Left(error) => error.printStackTrace()
      case Right(users) => {
        users.foreach(user => println(user.username))
      }
    }
  }

  private def listAllACLRules: Unit = {
    decode[List[MQTTACLRule]](httpRequest(aclURL, GET).body) match {
      case Left(error) => error.printStackTrace
      case Right(aclRules) => {
        printf(aclRulesHeaderFormat, "username", "topic", read, write)
        aclRules.foreach(aclRule =>
          printf(
            aclRulesHeaderFormat,
            aclRule.username,
            aclRule.topic,
            aclRule.read,
            aclRule.write
          )
        )
      }
    }
  }

  private def listACLRulesByUsername: Unit = {
    val username = StdIn.readLine(strUsername)
    decode[MQTTUserACLRules](httpRequest(userURL.format(username), GET).body) match {
      case Left(error) => error.printStackTrace()
      case Right(userRules) =>
        println(s"ACL rules of $username")
        printf(topicHeaderFormat, topic, read, write)
        userRules.acls.foreach(aclRule =>
          printf(
            topicHeaderFormat,
            aclRule.topic,
            aclRule.read,
            aclRule.write
          )
        )
    }
  }

  private def addACLRule: Unit = {
    val username = StdIn.readLine(strUsername)
    val topic = StdIn.readLine("Topic: ")
    val canRead = StdIn.readLine("Can read: ").toBoolean
    val canWrite = StdIn.readLine("Can write: ").toBoolean
    if (!addACLRuleImp(username, topic, canRead, canWrite)) {
      println(s"Can not add ACL rule. The username $username does not exist.")
    }
  }

  private def addACLRuleImp(
    username: String,
    topic: String,
    canRead: Boolean,
    canWrite: Boolean
  ): Boolean = {
    val data = MQTTACLRule.encoder.apply(
      MQTTACLRule(
        username,
        topic,
        canRead,
        canWrite
      )
    ).noSpaces
    HTTPNoContentCode == httpRequest(aclURL, POST, data).code
  }

  private def deleteACLRule: Unit = {
    val username: String = StdIn.readLine(strUsername)
    val topic: String = StdIn.readLine("topic: ")
    if (StdIn.readLine(areYouSure) == Y) {
      if (!deleteACLRuleImp(username, topic)) {
        println("username or topic name does not exist")
      }
    }
  }

  private def deleteACLRuleImp(username: String, topic: String): Boolean = {
    val data = MQTTACLRuleName.encoder.apply(
      MQTTACLRuleName(username, topic)
    ).noSpaces
    HTTPNoContentCode == httpRequest(aclURL, DELETE, data).code
  }

  private def deleteUser: Unit = {
    val username = StdIn.readLine(strUsername)
    if (StdIn.readLine(areYouSure) == Y) {
      if (HTTPNoContentCode != httpRequest(userURL.format(username), DELETE).code) {
        println(s"username $username does not exist")
      }
      else {
        deleteACLRuleImp(username, userTopic.format(username))
        deleteACLRuleImp(username, channelTopic.format("general"))
      }
    }
  }

  private def createUser: Unit = {
    val username = StdIn.readLine(strUsername)
    val password = StdIn.readLine("Password: ")
    val data = MQTTUser.encoder.apply(
      MQTTUser(username, password)
    ).noSpaces
    if (HTTPNoContentCode != httpRequest(userURL.format(""), POST, data).code) {
      println(s"username $username already exist.")
    }
    else {
      addACLRuleImp(username, userTopic.format(username), true, true)
      addACLRuleImp(username, channelTopic.format("general"), true, true)
    }
  }

  private def listAllChatChannel: Unit = {
    decode[MQTTUserACLRules](httpRequest(userURL.format(username), GET).body) match {
      case Left(error) =>
      case Right(userRules) =>
        println(s"all chanel of $username")
        printf("%-30s\t%-5s\t%-5s\n", channel, read, write)
        userRules.acls.foreach(
          acl => {
            val index = acl.topic.indexOf(channelTopic.format(""))
            if (index > -1) {
              printf(
                topicHeaderFormat,
                acl.topic.replace(channelTopic.format(""), ""),
                acl.read,
                acl.write
              )
            }
          }
        )
    }
  }

  private def createChatChannel(): Unit = {
    val channel = StdIn.readLine(strChannel)
    decode[List[MQTTUser]](httpRequest(userURL.format(""), GET).body) match {
      case Left(error) => error.printStackTrace
      case Right(users) => users.foreach(user =>
        addACLRuleImp(user.username, channelTopic, true, true)
      )
    }
  }

  private def deleteChatChannel: Unit = {
    val channel = StdIn.readLine(strChannel)
    if (StdIn.readLine(areYouSure) == Y) {
      decode[List[MQTTUser]](httpRequest(userURL, GET).body) match {
        case Left(error) => error.printStackTrace
        case Right(users) => users.foreach(user =>
          deleteACLRuleImp(user.username,
            channelTopic.format(channel)
          )
        )
      }
    }
  }

  private def banUserFromChatChannel: Unit = {
    val username: String = StdIn.readLine(strUsername)
    val channel: String = StdIn.readLine(strChannel)
    if (StdIn.readLine(areYouSure) == Y) {
      if (!deleteACLRuleImp(username, channelTopic.format(channel))) {
        println("username or topic name does not exist")
      }
    }
  }
}

object AdminClient {
  def apply(username: String, password: String): AdminClient = new AdminClient(username, password)
  private val topicHeaderFormat = "%-30s\t%-5s\t%-5s\n"
  private val aclRulesHeaderFormat = "%-10s\t%-30s\t%-5s\t%-5s\n"
  private val read = "read"
  private val write = "wirte"
  private val channel = "channel"
  private val topic = "topic"
}
