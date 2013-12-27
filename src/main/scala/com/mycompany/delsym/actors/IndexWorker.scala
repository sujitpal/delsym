package com.mycompany.delsym.actors

import com.mycompany.delsym.daos.MongoDbDao
import com.mycompany.delsym.daos.SolrPublisher
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import com.typesafe.config.ConfigFactory
import com.mycompany.delsym.daos.MockDbDao
import com.mycompany.delsym.daos.MockSolrPublisher

class IndexWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()
  
  val testUser = conf.getBoolean("delsym.testuser")
  val mongoDbDao = if (testUser) new MockDbDao()
                   else new MongoDbDao()
  val solrPublisher = if (testUser) new MockSolrPublisher() 
                      else new SolrPublisher()
  
  override def postStop() = solrPublisher.commit()
  
  def receive = {
    case m: Index => {
      index(m.url)
      sender ! IndexComplete(m.url, true)
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
              case Left(f) => log.error(f.e, f.msg)
              case _ => {}
            }
          }
        }
        case Left(f) => log.error(f.e, f.msg) 
      }
    } catch {
      case e: Exception => 
        log.error(e, "Error indexing URL:" + url)
    }
  }
}
