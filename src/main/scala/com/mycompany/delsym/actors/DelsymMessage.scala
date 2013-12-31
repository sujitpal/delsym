package com.mycompany.delsym.actors

import akka.actor.ActorRef
import spray.json._
import DefaultJsonProtocol._

sealed trait DelsymMessage

//////// messages sent from outside to controller /////////

case class Fetch(url: String, depth: Int, 
  metadata: Map[String,String]) extends DelsymMessage
  
case class Stats(stats: Map[String,Long]) extends DelsymMessage

case class Stop(x: Int) extends DelsymMessage

case class Register(ref: ActorRef) extends DelsymMessage

////////// messages between supervisor and worker //////////

case class Parse(url: String) extends DelsymMessage

case class Index(url: String) extends DelsymMessage

case class FetchComplete(url: String, fwd: Boolean) extends DelsymMessage

case class ParseComplete(url: String, fwd: Boolean) extends DelsymMessage

case class IndexComplete(url: String, fwd: Boolean) extends DelsymMessage

/////////////// Message <--> JSON ser/deser ////////////

object MessageProtocol extends DefaultJsonProtocol {
  implicit val fetchFormat = jsonFormat3(Fetch)
  implicit val parseFormat = jsonFormat1(Parse)
  implicit val indexFormat = jsonFormat1(Index)
  implicit val statsFormat = jsonFormat1(Stats)
  implicit val stopFormat = jsonFormat1(Stop)
}
