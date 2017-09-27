
// copyright

package uniq.mqttchat.client

import scala.io.StdIn

import uniq.networking.mqtt.base.BaseClient

class UserClient(username: String, password: String) extends BaseClient(username, password){
  override def login: Boolean = {
    super.login
  }

  override def printCommand: Unit = {
    println("1. join chanel")
    println("0. exit")
    val command = StdIn.readLine("select: ")
    command match {
      case "1" =>
      case "0" =>
    }
  }

}

object UserClient{
  def apply(username: String, password: String): UserClient = new UserClient(username, password)
}