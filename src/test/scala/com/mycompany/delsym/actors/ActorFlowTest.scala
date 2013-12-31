package com.mycompany.delsym.actors

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import java.util.Stack
import java.util.EmptyStackException
import akka.actor.Props
import com.mycompany.delsym.daos.MockCounters
import akka.actor.Actor
import com.typesafe.config.ConfigFactory

class ActorFlowTest(sys: ActorSystem) 
    extends TestKit(sys) 
    with FunSuite
    with BeforeAndAfterAll 
    with ImplicitSender {
  
  def this() = this(ActorSystem("DelsymTest"))
  
  test("test message flow across actors") {
    val controller = 
      system.actorOf(Props[Controller], "controller")
    val numMessages = 10
    (0 until numMessages).foreach(i => {
      controller ! Fetch(i.toString, 0, Map())
    })
    controller ! Stop(0)
    system.awaitTermination
    Console.println("Counters=" + List(
      MockCounters.fetched.longValue(),
      MockCounters.parsed.longValue(),
      MockCounters.indexed.longValue(),
      MockCounters.dbFetched.longValue(),
      MockCounters.dbParsed.longValue(),
      MockCounters.dbIndexed.longValue(),
      MockCounters.outlinkCalled.longValue()))
    assert(MockCounters.fetched.longValue() == numMessages)
    assert(MockCounters.parsed.longValue() == numMessages)
    assert(MockCounters.indexed.longValue() == numMessages)
    assert(MockCounters.dbFetched.longValue() == numMessages)
    assert(MockCounters.dbParsed.longValue() == numMessages)
    assert(MockCounters.dbIndexed.longValue() == numMessages)
    assert(MockCounters.outlinkCalled.longValue() == numMessages)
  }
}