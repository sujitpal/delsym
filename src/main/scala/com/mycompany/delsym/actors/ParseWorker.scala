package com.mycompany.delsym.actors

import com.mycompany.delsym.daos.MongoDbDao
import com.mycompany.delsym.daos.TikaParser
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.actorRef2Scala
import com.mycompany.delsym.daos.MockDbDao
import com.mycompany.delsym.daos.MockParser


class ParseWorker extends Actor with ActorLogging {

  val conf = ConfigFactory.load()

  val testUser = conf.getBoolean("delsym.testuser")
  val mongoDbDao = if (testUser) new MockDbDao()
                   else new MongoDbDao()
  val parser = if (testUser) new MockParser()
               else new TikaParser()

  def receive = {
    case m: Parse => {
      parse(m.url)
      sender ! ParseComplete(m.url)
    }
    case _ => log.info("Unknown message received.")
  }
  
  def parse(url: String): Unit = {
    log.info("Parsing URL: {}", url)
    try {
      mongoDbDao.getByUrl(url, List.empty) match {
        case Right(row) => {
          if (! row.isEmpty) {
            val content = row("content").asInstanceOf[String]
            parser.parse(url, content) match {
              case Right(textmeta) => {
                mongoDbDao.insertParsed(
                    url, textmeta._1, textmeta._2) match {
                  case Left(f) => log.error(f.e, f.msg)
                  case _ => {}
                }
              }
              case Left(f) => log.error(f.e, f.msg)
            }
          }
        }
        case Left(f) => log.error(f.e, f.msg)
      }
    } catch {
      case e: Exception => 
        log.error(e, "Error parsing URL: " + url)
    }
  }
}
