package com.mycompany.delsym.actors

import scala.concurrent.duration.DurationInt

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter

class Controller extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy(
      maxNrOfRetries = 10,
      withinTimeRange = 1.minute) {
    case _: Exception => SupervisorStrategy.Restart
  }
  
  val reaper = context.actorOf(Props[Reaper], name="reaper")

  val config = ConfigFactory.load()
  val numFetchers = config.getInt("delsym.fetchers.numworkers")
  val numParsers = config.getInt("delsym.parsers.numworkers")
  val numIndexers = config.getInt("delsym.indexers.numworkers")
  val queueSizes = scala.collection.mutable.Map[String,Int]()
  
  val restartChild = OneForOneStrategy() {
    case e: Exception => SupervisorStrategy.Restart
  }
  val fetchers = context.actorOf(Props[FetchWorker]
    .withRouter(RoundRobinRouter(nrOfInstances=numFetchers, 
    supervisorStrategy=restartChild)), 
    name="fetchers")
  reaper ! Register(fetchers)
  queueSizes += (("fetchers", 0))
  
  val parsers = context.actorOf(Props[ParseWorker]
    .withRouter(RoundRobinRouter(nrOfInstances=numParsers, 
    supervisorStrategy=restartChild)), 
    name="parsers")
  reaper ! Register(parsers)
  queueSizes += (("parsers", 0))
  
  val indexers = context.actorOf(Props[IndexWorker]
    .withRouter(RoundRobinRouter(nrOfInstances=numIndexers,
    supervisorStrategy=restartChild)),
    name="indexers")
  reaper ! Register(indexers)
  queueSizes += (("indexers", 0))
    
  def receive = {
    case m: Fetch => {
      increment("fetchers")
      fetchers ! m
    }
    case m: FetchComplete => {
      decrement("fetchers")
      parsers ! Parse(m.url)
    }
    case m: Parse => {
      increment("parsers")
      parsers ! m
    }
    case m: ParseComplete => {
      decrement("parsers")
      outlinks(m.url).map(outlink => 
        fetchers ! Fetch(outlink._1, outlink._2, outlink._3))
      indexers ! Index(m.url)
    }
    case m: Index => {
      increment("indexers")
      indexers ! m
    }
    case m: IndexComplete => {
      decrement("indexers")
    }
    case m: Stats => sender ! queueSize()
    case Stop => reaper ! Stop
    case _ => log.info("Unknown message received.")
  }
  
  def queueSize(): Stats = Stats(queueSizes.toMap)
  
  def outlinks(id: String): 
      List[(String,Int,Map[String,Any])] = {
    log.info("TODO: Fetch outlinks for id:{}", id)
    List()
  }
  
  def increment(key: String): Unit = {
    queueSizes += ((key, queueSizes(key) + 1))
  }
  
  def decrement(key: String): Unit = {
    queueSizes += ((key, queueSizes(key) - 1))
  }
}
