package com.mycompany.delsym.actors

import scala.collection.JavaConversions.asScalaBuffer
import scala.concurrent.duration.DurationInt

import com.mycompany.delsym.daos.HtmlOutlinkFinder
import com.mycompany.delsym.daos.MockOutlinkFinder
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.AddressFromURIString
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.actor.actorRef2Scala
import akka.remote.routing.RemoteRouterConfig
import akka.routing.RoundRobinRouter
import akka.routing.RouterConfig

class Controller extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy(
      maxNrOfRetries = 10,
      withinTimeRange = 1.minute) {
    case _: Exception => SupervisorStrategy.Restart
  }
  
  val reaper = context.actorOf(Props[Reaper], name="reaper")

  val conf = ConfigFactory.load()
  val numFetchers = conf.getInt("delsym.fetchers.numworkers")
  val fetchNodes = conf.getList("delsym.fetchers.nodes")
  
  val numParsers = conf.getInt("delsym.parsers.numworkers")
  val parseNodes = conf.getList("delsym.parsers.nodes")
  
  val numIndexers = conf.getInt("delsym.indexers.numworkers")
  val indexNodes = conf.getList("delsym.indexers.nodes")
  
  val testUser = conf.getBoolean("delsym.testuser")
  val outlinkFinder = if (testUser) new MockOutlinkFinder()
                      else new HtmlOutlinkFinder()
  
  val queueSizes = scala.collection.mutable.Map[String,Long]()
  
  val fetchers = context.actorOf(Props[FetchWorker]
    .withRouter(buildRouter(numFetchers, fetchNodes)), 
    name="fetchers")
  reaper ! Register(fetchers)
  queueSizes += (("fetchers", 0L))

  val parsers = context.actorOf(Props[ParseWorker]
    .withRouter(buildRouter(numParsers, parseNodes)), 
    name="parsers")
  reaper ! Register(parsers)
  queueSizes += (("parsers", 0L))
  
  val indexers = context.actorOf(Props[IndexWorker]
    .withRouter(buildRouter(numIndexers, indexNodes)),
    name="indexers")
  reaper ! Register(indexers)
  queueSizes += (("indexers", 0L))

  def receive = {
    case m: Fetch => {
      increment("fetchers")
      fetchers ! m
    }
    case m: FetchComplete => {
      decrement("fetchers")
      if (m.fwd) parsers ! Parse(m.url)
    }
    case m: Parse => {
      increment("parsers")
      parsers ! m
    }
    case m: ParseComplete => {
      decrement("parsers")
      outlinks(m.url).map(outlink => 
        fetchers ! Fetch(outlink._1, outlink._2, outlink._3))
      if (m.fwd) indexers ! Index(m.url)
    }
    case m: Index => {
      increment("indexers")
      indexers ! m
    }
    case m: IndexComplete => {
      decrement("indexers")
    }
    case m: Stats => {
      sender ! queueSize()
    }
    case m: Stop => {
      reaper ! Stop(0)
    }
    case _ => log.info("Unknown message received.")
  }
  
  def buildRouter(n: Int, nodes: ConfigList): RouterConfig = {
    if (nodes.isEmpty) RoundRobinRouter(n)
    else {
      val addrs = nodes.unwrapped()
        .map(node => node.asInstanceOf[String])
        .map(node => AddressFromURIString(node))
        .toSeq
      RemoteRouterConfig(RoundRobinRouter(n), addrs)
    }
  }
  
  def queueSize(): Stats = Stats(queueSizes.toMap)
  
  def outlinks(url: String): 
      List[(String,Int,Map[String,String])] = {
    outlinkFinder.findOutlinks(url) match {
      case Right(triples) => triples
      case Left(f) => List.empty
    }
  }
  
  def increment(key: String): Unit = {
    queueSizes += ((key, queueSizes(key) + 1))
  }
  
  def decrement(key: String): Unit = {
    queueSizes += ((key, queueSizes(key) - 1))
  }
}
