package com.mycompany.delsym.actors

import akka.actor.ActorLogging
import akka.actor.Actor
import java.util.concurrent.atomic.AtomicLong
import akka.actor.Terminated
import scala.collection.mutable.ArrayBuffer
import akka.actor.ActorRef
import akka.routing.Broadcast
import akka.actor.PoisonPill

class Reaper extends Actor with ActorLogging {

  val refs = ArrayBuffer[ActorRef]()
  
  def receive = {
    case Register(ref) => {
      context.watch(ref)
      refs += ref
    }
    case Stop(_) => {
      refs.head ! Broadcast(PoisonPill)
    }
    case Terminated(ref) => {
      val tail = refs.tail
      if (tail.isEmpty) context.system.shutdown
      else {
        refs.clear
        refs ++= tail
        refs.head ! Broadcast(PoisonPill)
      }
    }
    case _ => log.info("Unknown message received.")
  }
}