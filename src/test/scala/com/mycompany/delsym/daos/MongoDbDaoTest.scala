package com.mycompany.delsym.daos

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import java.util.Date

class MongoDbDaoTest extends FunSuite 
                     with BeforeAndAfterAll {

  val mongoDbDao = new MongoDbDao()
  
  override def afterAll() = mongoDbDao.close()
  
  test("insert after fetch") {
    val url = "http://www.google.com"
    val content = "Some arbitary content"
    val fetchMetadata = List(
       ("feed_title", "Some arbitary title"),
       ("feed_summary", "Jack the giant killer")).toMap
    mongoDbDao.insertFetched(url, 0, fetchMetadata, content)
    mongoDbDao.getByUrl(url, List.empty) match {
      case Right(row) => {
        Console.println("row (after fetch)=" + row)
        assert(row != null)
        assert(row.size == 7)
      }
      case Left(f) => {
        Console.println("ERROR:" + f.msg)
        f.e.printStackTrace()
      }
    }
  }
  
  test("insert after parse") {
    val url = "http://www.google.com"
    val text = "Text of the page"
    val parseMetadata = List(
      ("author" -> "Some arbitary_author"),
      ("title" -> "Some arbitary title")).toMap
    mongoDbDao.insertParsed(url, text, parseMetadata)
    mongoDbDao.getByUrl(url, List.empty) match {
      case Right(row) => {
        Console.println("row (after parse)=" + row)
        assert(row != null)
        assert(row.size == 11)
      }
      case Left(f) => {
        Console.println("ERROR:" + f.msg)
        f.e.printStackTrace()
      } 
    }
  }
  
  test("insert after index") {
    val url = "http://www.google.com"
    mongoDbDao.insertIndexed(url)
    mongoDbDao.getByUrl(url, List.empty) match {
      case Right(row) => {
        Console.println("row (after index)=" + row)
        assert(row != null)
        assert(row.size == 12)
      }
      case Left(f) => {
        Console.println("ERROR:" + f.msg)
        f.e.printStackTrace()
      }
    }
  }
  
  test("get fetch time as long") {
    val url = "http://www.google.com"
    mongoDbDao.getByUrl(url, List("fts")) match {
      case Right(row) => {
        val fts = row.get("fts") match {
          case Some(x: Long) => x
          case _ => -1L 
        }
        Console.println("fetch timestamp=" + fts)
        assert(fts > 0L)
      }
      case Left(f) => {
        Console.println("ERROR:" + f.msg)
        f.e.printStackTrace()
      }
    }
  }
}