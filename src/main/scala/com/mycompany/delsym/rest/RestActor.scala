package com.mycompany.delsym.rest

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import com.mycompany.delsym.actors.Controller
import com.mycompany.delsym.actors.Fetch
import com.mycompany.delsym.actors.Index
import com.mycompany.delsym.actors.MessageProtocol
import com.mycompany.delsym.actors.Parse
import com.mycompany.delsym.actors.Stats
import com.mycompany.delsym.actors.Stop
import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.json.pimpAny
import spray.routing.Directive.pimpApply
import spray.routing.HttpServiceActor

class RestActor extends Actor with HttpServiceActor {

  val conf = ConfigFactory.load()
  implicit val timeout = Timeout(
    conf.getInt("delsym.rest.timeout").seconds)

  val controller = context.actorOf(Props[Controller], 
    "controller")
  
  def stop() = controller ! Stop
  
  def receive = runRoute {
    (get & path("stats")) {
      jsonpWithParameter("callback") {
        complete {
          val future = (controller ? Stats(Map.empty))
            .mapTo[Stats]
          val result = Await.result(future, timeout.duration)
          import MessageProtocol.statsFormat
          result.toJson.prettyPrint
        }
      }
    } ~
    (put & path("fetch")) { 
      jsonpWithParameter("callback") {
        import MessageProtocol.fetchFormat
        entity(as[Fetch]) { fetch => 
          complete {
            controller ! fetch
            "Got(" + fetch.toJson.compactPrint + ")"
          }  
        }
      }
    } ~
    (put & path("parse")) { 
      jsonpWithParameter("callback") {
        import MessageProtocol.parseFormat
        entity(as[Parse]) { parse => 
          complete {
            controller ! parse
            "Got(" + parse.toJson.compactPrint + ")"
          }  
        }
      }
    } ~
    (put & path("index")) { 
      jsonpWithParameter("callback") {
        import MessageProtocol.indexFormat
        entity(as[Index]) { index => 
          complete {
            controller ! index
            "Got(" + index.toJson.compactPrint + ")"
          }  
        }
      }
    } ~
    (get & path("stop")) { 
      complete {
        import MessageProtocol.stopFormat
        controller ! Stop(0)
        "Stop signal sent"
      }
    }
  }
}