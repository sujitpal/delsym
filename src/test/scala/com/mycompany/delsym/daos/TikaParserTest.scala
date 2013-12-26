package com.mycompany.delsym.daos

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite

class TikaParserTest extends FunSuite
                     with BeforeAndAfterAll {

  val httpFetcher = new HttpFetcher()
  val tikaParser = new TikaParser()
  
  test("Parse HTML document with TikaParser") {
    val url = "http://sujitpal.blogspot.com" 
    httpFetcher.fetch(url) match {
      case Right(content) => {
        tikaParser.parse(url, content) match {
          case Right(tm) => {
            Console.println("text=" + tm._1)
            assert(tm._1 != null)
            assert(tm._1.indexOf("Salmon Run") > -1)
            Console.println("metadata=" + tm._2)
            assert(tm._2.contains("title"))
          } 
          case Left(f) => {
            Console.println("FAILURE!! " + f.msg)
            f.e.printStackTrace()
          }
        }
      }
      case Left(f) => {
        Console.println("FAILURE!! " + f.msg)
        f.e.printStackTrace()
      }
    }   
  }
}