package com.mycompany.delsym.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

class FetchWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()
  
  def receive = {
    case m: Fetch => {
      val id = fetchAndStore(m.url, m.depth, m.metadata)
      sender ! FetchComplete(id)
    }
    case _ => log.info("Unknown message.")
  }

  def fetchAndStore(url: String, depth: Int, 
      metadata: Map[String,Any]): String = {
    log.info("TODO: fetching URL:{}", url)
    url
  }
}