package com.mycompany.delsym.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

class IndexWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()
  
  def receive = {
    case m: Index => {
      index(m.id)
      sender ! IndexComplete(m.id)
    }
    case _ => log.info("Unknown message received.")
  }

  def index(id: String): Unit = {
    log.info("TODO: indexing id:{}", id)
  }
}