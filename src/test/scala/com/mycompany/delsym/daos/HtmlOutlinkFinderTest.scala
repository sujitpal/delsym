package com.mycompany.delsym.daos

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite

class HtmlOutlinkFinderTest extends FunSuite
                            with BeforeAndAfterAll {

  val httpFetcher = new HttpFetcher()
  val outlinkFinder = new HtmlOutlinkFinder()
  
  test("find outlinks from web page") {
    httpFetcher.fetch("http://sujitpal.blogspot.com") match {
      case Right(content) =>
        outlinkFinder.findOutlinks(
          "http://sujitpal.blogspot.com", content) match {
          case Right(links) => {
            Console.println(links)
            assert(links != null)
            assert(links.size > 0)
            assert(links
              .filter(_.indexOf("sujitpal.blogspot.com") > -1)
              .size > 0)  
          }
          case Left(f) => 
            Console.println("FAILURE!! " + f.msg, f.e)
        }
      case Left(f) => 
        Console.println("FAILURE!! " + f.msg, f.e)
    }
  }
}