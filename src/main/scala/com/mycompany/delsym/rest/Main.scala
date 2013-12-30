package com.mycompany.delsym.rest

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import spray.can.Http
import spray.httpx.RequestBuilding.Get

object Main extends App {

  implicit val system = ActorSystem("DelSym")
  
  val conf = ConfigFactory.load()
  val host = conf.getString("delsym.rest.host")
  val port = conf.getInt("delsym.rest.port")

  val api = system.actorOf(Props[RestActor], "api")
  IO(Http) ! Http.Bind(api, host, port = port)
  
  sys.addShutdownHook {
    Console.println("Shutting down...")
    api ! Get("/stop")
  }
}
