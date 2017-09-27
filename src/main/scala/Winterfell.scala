// Copyright (C) 2016-2017 Ark Maxim, Inc.

import uniq.mqttchat.client.AdminClient
import uniq.mqttchat.client.UserClient

import scala.io.StdIn

object Winterfell {

  def main(args: Array[String]): Unit = {
    println(Console.YELLOW + "\nWelcome to Winterfell.")
    val username = StdIn.readLine("username: ")
    val password = StdIn.readLine("password: ")
    val client = username match {
      case "admin" => AdminClient(username, password)
      case _ => UserClient(username, password)
    }

    if (client.login) {
      client.printCommand
    } else {
      println("an error occurred during logging in")
    }
    client.disconnect
    println("Press Enter to exit.")
    StdIn.readLine()
    System.exit(0)
  }
}
