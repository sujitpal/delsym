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

class ActorFlowTest(sys: ActorSystem) 
    extends TestKit(sys) 
    with FunSuite
    with BeforeAndAfterAll 
    with ImplicitSender {
  
  def this() = this(ActorSystem("DelsymTest"))
  
  test("test message flow across actors") {
    val controller = 
      system.actorOf(Props[Controller], "controller")
    (0 until 10).foreach(i => {
      controller ! Fetch(i.toString, 0, Map())
    })
    controller ! Stop
    system.awaitTermination
    assert(MockCounters.fetched.longValue() == 10L)
    assert(MockCounters.parsed.longValue() == 10L)
    assert(MockCounters.indexed.longValue() == 10L)
    assert(MockCounters.dbFetched.longValue() == 10L)
    assert(MockCounters.dbParsed.longValue() == 10L)
    assert(MockCounters.dbIndexed.longValue() == 10L)
    assert(MockCounters.outlinkCalled.longValue() == 0L)
  }
}