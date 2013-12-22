package com.mycompany.delsym.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

class ParseWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()
  
  def receive = {
    case m: Parse => {
      parse(m.id)
      sender ! ParseComplete(m.id)
    }
    case _ => log.info("Unknown message received.")
  }
  
  def parse(id: String): String = {
    log.info("TODO: Parsing data for id:{}", id)
    "TODO"
  }
}