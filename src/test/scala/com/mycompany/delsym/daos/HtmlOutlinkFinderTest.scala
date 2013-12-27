package com.mycompany.delsym.daos

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite

class HtmlOutlinkFinderTest extends FunSuite
                            with BeforeAndAfterAll {

  val httpFetcher = new HttpFetcher()
  val mongoDbDao = new MongoDbDao()
  val outlinkFinder = new HtmlOutlinkFinder()
  
  test("find outlinks from web page") {
    val url = "http://sujitpal.blogspot.com"
    httpFetcher.fetch(url) match {
      case Right(content) =>
        mongoDbDao.insertFetched(url, 1, Map.empty, content)
        outlinkFinder.findOutlinks(url) match {
          case Right(triples) => {
            assert(triples != null)
            assert(triples.size > 0)
            assert(triples.head._2 == 0)
          }
          case _ => {}
        }
      case Left(f) => 
        Console.println("FAILURE!! " + f.msg, f.e)
    }
  }
}