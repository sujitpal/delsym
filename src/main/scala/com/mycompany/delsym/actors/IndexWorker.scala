package com.mycompany.delsym.actors

import com.mycompany.delsym.daos.MongoDbDao
import com.mycompany.delsym.daos.SolrPublisher

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala

class IndexWorker extends Actor with ActorLogging {

  val mongoDbDao = new MongoDbDao()
  val solrPublisher = new SolrPublisher()
  
  override def postStop() = solrPublisher.commit()
  
  def receive = {
    case m: Index => {
      index(m.url)
      sender ! IndexComplete(m.url)
    }
    case _ => log.info("Unknown message received.")
  }

  def index(url: String): Unit = {
    log.info("Indexing URL: {}", url)
    try {
      mongoDbDao.getByUrl(url, List.empty) match {
        case Right(row) => {
          if (! row.isEmpty) {
            solrPublisher.publish(url, row)
            mongoDbDao.insertIndexed(url) match {
              case Left(f) => log.error(f.msg, f.e)
              case _ => {}
            }
          }
        }
        case Left(f) => log.error(f.msg, f.e) 
      }
    } catch {
      case e: Exception => 
        log.error("Error indexing URL: {}", url, e)
    }
  }
}