package com.mycompany.delsym.remote

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object RemoteAkka extends App {

  val name = if (args.isEmpty) "remote" else args(0)
  
  val conf = ConfigFactory.load("remote")
  val host = conf.getString("akka.remote.netty.tcp.hostname")
  val port = conf.getInt("akka.remote.netty.tcp.port")
  
  val system = ActorSystem(name, conf)
  Console.println("Remote system [%s] listening on %s:%d"
    .format(name, host, port))
  
  sys.addShutdownHook {
    Console.println("Shutting down Remote Akka")
    system.shutdown
  }
}