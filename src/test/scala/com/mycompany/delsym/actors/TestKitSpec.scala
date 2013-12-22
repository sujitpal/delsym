package com.mycompany.delsym.actors

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import java.util.Stack
import java.util.EmptyStackException

abstract class TestKitSpec(name: String) 
    extends TestKit(ActorSystem(name))
    with FunSuite
    with BeforeAndAfterAll
    with ImplicitSender {

  override def afterAll() = system.shutdown

}

class ActorFlowTest extends TestKitSpec("Delsym") {
  
  test("pop invoked on non-empty stack") {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    val oldSize = stack.size
    val result = stack.pop
    assert(result === 2)
    assert(stack.size === oldSize - 1)
  }

  test("pop is invoked on an empty stack") {
    val emptyStack = new Stack[Int]
    intercept[EmptyStackException] {
      emptyStack.pop
    }
    assert(emptyStack.isEmpty)
  }
}