package com.mycompany.delsym.actors

import scala.concurrent.duration.DurationInt

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite

import com.mycompany.delsym.daos.MockCounters

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.util.Timeout

class ActorFlowTest(sys: ActorSystem) 
    extends TestKit(sys) 
    with FunSuite
    with BeforeAndAfterAll 
    with ImplicitSender {
  
  def this() = this(ActorSystem("DelsymTest"))
  implicit val timeout = Timeout(5.seconds)

  // this does not work in remote mode, since the
  // DAOs are called in a different Akka JVM and
  // update counters there, where the unit test 
  // cannot access the numbers (but it can be used
  // to verify that the remote configuration works
  // with our test case by commenting out the asserts)
  
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
//    assert(MockCounters.fetched.longValue() == numMessages)
//    assert(MockCounters.parsed.longValue() == numMessages)
//    assert(MockCounters.indexed.longValue() == numMessages)
//    assert(MockCounters.dbFetched.longValue() == numMessages)
//    assert(MockCounters.dbParsed.longValue() == numMessages)
//    assert(MockCounters.dbIndexed.longValue() == numMessages)
//    assert(MockCounters.outlinkCalled.longValue() == numMessages)
  }
}