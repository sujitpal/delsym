package com.mycompany.delsym.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object RemoteAkka extends App {

  val conf = ConfigFactory.load("remote")
  val host = conf.getString("akka.remote.netty.tcp.hostname")
  val port = conf.getInt("akka.remote.netty.tcp.port")
  
  val system = ActorSystem("RemoteAkka", conf)
  Console.println("Remote Akka listening on %s:%d"
    .format(host, port))
  
  sys.addShutdownHook {
    Console.println("Shutting down Remote Akka")
    system.shutdown
  }
}