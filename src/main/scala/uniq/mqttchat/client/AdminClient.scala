// Copyright (C) 2016-2017 Ark Maxim, Inc.

package uniq.mqttchat.client

// scalastyle:off underscore.import
import scala.io.StdIn

import uniq.networking.mqtt.base.BaseClient
import uniq.networking.mqtt.base.BaseClient._
import uniq.networking.mqtt.base.model.MQTTACLRule
import uniq.networking.mqtt.base.model.MQTTACLRuleName
import uniq.networking.mqtt.base.model.MQTTUser
import uniq.networking.mqtt.base.model.MQTTUserACLRules
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
    println("8. Create chat channel")
    println("9. Delete a chat channel")
    println("10 Ban user from a chat channel")
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
      case "8" => createChatChannel
      case "9" => deleteChatChannel
      case "10" => banUserFromChatChannel
      case _ =>
        this.disconnect
        System.exit(0)
    }
    println("Press any key to continue...")
    StdIn.readLine()
    printCommand
  }
// scalastyle:on cyclomatic.complexity

  private def getListAllUsers: List[MQTTUser] = {
    val result = httpRequest(userURL.format(""), GET)
    io.circe.parser.decode[List[MQTTUser]](result.body).toTry.get
  }

  private def listAllUsers: Unit = {
    getListAllUsers.foreach(user => println(user.username))
  }

  private def listAllACLRules: Unit = {
    val result = httpRequest(aclURL, GET)
    val listUser = io.circe.parser.decode[List[MQTTACLRule]](result.body).toTry.get
    printf("%-10s\t%-30s\t%-5s\t%-5s\n", "username", "topic", "read", "write")
    listUser.foreach(x =>
      printf(
        "%-10s\t%-30s\t%-5s\t%-5s\n",
        x.username,
        x.topic,
        x.read,
        x.write
      )
    )
  }

  private def listACLRulesByUsername: Unit = {
    val username = StdIn.readLine(strUsername)
    val result = httpRequest(userURL.format(username), GET)
    if (result.code != 200) {
      println(result.body)
    }
    else {
      val userRules = io.circe.parser.decode[MQTTUserACLRules](result.body).toTry.get
      println(s"ACL rules of $username")
      printf("%-30s\t%-5s\t%-5s\n", "topic", "read", "write")
      userRules.acls.foreach(x => printf("%-30s\t%-5s\t%-5s\n", x.topic, x.read, x.write))
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
    val result = httpRequest(aclURL, POST, data)
    HTTPNoContentCode == result.code
  }

  private def deleteACLRule: Unit = {
    val username: String = StdIn.readLine(strUsername)
    val topic: String = StdIn.readLine("topic: ")
    val confirm = StdIn.readLine(areYouSure)
    if (confirm.toUpperCase == Y) {
      if (!deleteACLRuleImp(username, topic)) {
        println("username or topic name does not exist")
      }
    }
  }

  private def deleteACLRuleImp(username: String, topic: String): Boolean = {
    val data = MQTTACLRuleName.encoder.apply(
      MQTTACLRuleName(username, topic)
    ).noSpaces
    val result = httpRequest(aclURL, DELETE, data)
    HTTPNoContentCode == result.code
  }

  private def deleteUser: Unit = {
    val username = StdIn.readLine(strUsername)
    val confirm = StdIn.readLine(areYouSure)
    if (confirm.toUpperCase == "Y") {
      val result = httpRequest(userURL.format(username), DELETE)
      println(result.body)
      if (HTTPNoContentCode != result.code) {
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
    val result = httpRequest(userURL.format(""), POST, data)
    if (HTTPNoContentCode != result.code) {
      println(s"username $username already exist.")
    }
    else {
      addACLRuleImp(username, userTopic.format(username), true, true)
      addACLRuleImp(username, channelTopic.format("general"), true, true)
    }
  }

  private def createChatChannel: Unit = {
    val channel = StdIn.readLine(strChannel)
    getListAllUsers.foreach(user =>
      addACLRuleImp(
        user.username,
        channelTopic.format(channel),
        true,
        true
      )
    )
  }

  private def deleteChatChannel: Unit = {
    val channel = StdIn.readLine(strChannel)
    val confirm = StdIn.readLine(areYouSure)
    if (confirm.toUpperCase == Y) {
      getListAllUsers.foreach(user =>
        deleteACLRuleImp(
          user.username,
          channelTopic.format(channel)
        )
      )
    }
  }

  private def banUserFromChatChannel: Unit = {
    val username: String = StdIn.readLine(strUsername)
    val channel: String = StdIn.readLine(strChannel)
    val confirm = StdIn.readLine(areYouSure)
    if (confirm.toUpperCase == Y) {
      if (!deleteACLRuleImp(username, channelTopic.format(channel))) {
        println("username or topic name does not exist")
      }
    }
  }
}

object AdminClient {
  def apply(username: String, password: String): AdminClient = new AdminClient(username, password)
}
