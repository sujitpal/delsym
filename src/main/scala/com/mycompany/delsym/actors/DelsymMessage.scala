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

case class Parse(url: String) extends DelsymMessage

case class Index(url: String) extends DelsymMessage

case class FetchComplete(url: String, fwd: Boolean) extends DelsymMessage

case class ParseComplete(url: String, fwd: Boolean) extends DelsymMessage

case class IndexComplete(url: String, fwd: Boolean) extends DelsymMessage
