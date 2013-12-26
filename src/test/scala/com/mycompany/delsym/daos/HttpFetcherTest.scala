package com.mycompany.delsym.daos

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite

class HttpFetcherTest extends FunSuite 
                      with BeforeAndAfterAll {

  val httpFetcher = new HttpFetcher()
  
  test("fetch a random page") {
    httpFetcher.fetch("http://sujitpal.blogspot.com") match {
      case Right(content) => {
        Console.println("content=" + content)
        assert(content != null)
        assert(content.indexOf("sujitpal.blogspot.com") > -1)
      }
      case Left(f) => Console.println("Failure!!" + f.msg, f.e)
    }
  }
  
  test("fetch random HTML page and parse outlinks") {
    httpFetcher.fetch("http://sujitpal.blogspot.com") match {
      case Right(content) => {
        //  

        Console.println("content=" + content)
        assert(content != null)
        assert(content.indexOf("sujitpal.blogspot.com") > -1)
      }
      case Left(f) => Console.println("Failure!!" + f.msg, f.e)
    }
  }
}