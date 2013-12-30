package com.mycompany.delsym.rest

import com.typesafe.config.ConfigFactory

import akka.actor.Props
import akka.actor.actorRef2Scala
import spray.can.server.SprayCanHttpServerApp
import spray.httpx.RequestBuilding.Get

object Main extends App with SprayCanHttpServerApp {
 
  val conf = ConfigFactory.load()
  val host = conf.getString("delsym.rest.host")
  val port = conf.getInt("delsym.rest.port")

  val api = system.actorOf(Props[RestActor], "api")
  newHttpServer(api) ! Bind(interface = host, port = port)
  
  sys.addShutdownHook {
    Console.println("Shutting down...")
    api ! Get("/stop")
  }
}
