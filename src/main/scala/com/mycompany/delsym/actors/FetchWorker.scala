package com.mycompany.delsym.actors

import java.util.Date
import com.mycompany.delsym.daos.HttpFetcher
import com.mycompany.delsym.daos.MongoDbDao
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import com.mycompany.delsym.daos.MockDbDao
import com.mycompany.delsym.daos.MockHttpFetcher

class FetchWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()
  
  val testUser = conf.getBoolean("delsym.testuser")
  val mongoDbDao = if (testUser) new MockDbDao() 
                   else new MongoDbDao()
  val httpFetcher = if (testUser) new MockHttpFetcher() 
                    else new HttpFetcher()
                    
  
  val refreshInterval = conf.getLong(
    "delsym.fetchers.refreshIntervalDays") * 8640000L
  
  def receive = {
    case m: Fetch => {
      if (shouldFetch(m.url)) {
        log.info("Fetching URL: {}", m.url)
        httpFetcher.fetch(m.url) match {
          case Left(f) => log.error(f.e, f.msg)
          case Right(content) => 
            mongoDbDao.insertFetched(
              m.url, m.depth, m.metadata, content) match {
              case Left(f) => log.error(f.e, f.msg)
              case _ => {}
            }
        }
        sender ! FetchComplete(m.url, true)
      } else sender ! FetchComplete(m.url, false)
    }
    case _ => log.info("Unknown message.")
  }

  /**
   * Return true if id does not exist or if date
   * fetched is too far into the past.
   */
  def shouldFetch(url: String): Boolean = {
    val current = new Date().getTime()
    val lastFetched = mongoDbDao.getByUrl(
        url, List("fts")) match {
      case Right(row) => row.getOrElse("fts", current)
                            .asInstanceOf[Long]
      case Left(f) => log.error(f.e, f.msg); current
    }
    lastFetched + refreshInterval > current
  }
}