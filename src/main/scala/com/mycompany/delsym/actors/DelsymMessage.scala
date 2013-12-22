package com.mycompany.delsym.actors

import akka.actor.ActorRef

sealed trait DelsymMessage

//////// messages sent from outside to controller /////////

case class Fetch(url: String, depth: Int, 
  metadata: Map[String,Any]) extends DelsymMessage
  
case class Stats(stats: Map[String,Int]) extends DelsymMessage

case object Stop extends DelsymMessage

case class Register(ref: ActorRef) extends DelsymMessage

////////// messages between supervisor and worker //////////

case class Parse(id: String) extends DelsymMessage

case class Index(id: String) extends DelsymMessage

case class FetchComplete(id: String) extends DelsymMessage

case class ParseComplete(id: String) extends DelsymMessage

case class IndexComplete(id: String) extends DelsymMessage
