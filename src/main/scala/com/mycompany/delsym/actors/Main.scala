package com.mycompany.delsym.actors

import akka.actor.Props
import akka.actor.ActorSystem

object Main extends App {
  val system = ActorSystem("DelsymTest")
  val controller = system.actorOf(Props[Controller], "controller")
  
  (0 until 100).foreach(i => {
    if (i == 50) controller ! Stats(null)
    controller ! Fetch(i.toString, 0, Map())
  })
  controller ! Stop
//  system.shutdown
}