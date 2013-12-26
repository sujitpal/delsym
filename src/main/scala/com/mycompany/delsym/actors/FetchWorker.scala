package com.mycompany.delsym.actors

import java.util.Date

import com.mycompany.delsym.daos.HttpFetcher
import com.mycompany.delsym.daos.MongoDbDao
import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala

class FetchWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()
  
  val mongoDbDao = new MongoDbDao()
  val httpFetcher = new HttpFetcher()
  
  val refreshInterval = conf.getLong(
    "delsym.fetchers.refreshIntervalDays") * 8640000L
  
  def receive = {
    case m: Fetch => {
      if (shouldFetch(m.url)) {
        log.info("Fetching URL: {}", m.url)
        httpFetcher.fetch(m.url) match {
          case Left(f) => log.error(f.msg, f.e)
          case Right(content) => 
            mongoDbDao.insertFetched(
              m.url, m.depth, m.metadata, content) match {
              case Left(f) => log.error(f.msg, f.e)
              case _ => {}
            }
        }
      }
      sender ! FetchComplete(m.url)
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
      case Left(f) => log.error(f.msg, f.e); current
    }
    lastFetched + refreshInterval > current
  }
//  def fetchAndStore(url: String, depth: Int, 
//      metadata: Map[String,Any]): String = {
//    log.info("TODO: fetching URL:{} for {}:{}", url, self.path.parent.name, self.path.name)
//    url
//  }
}