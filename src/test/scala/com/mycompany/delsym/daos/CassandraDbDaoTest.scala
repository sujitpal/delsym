package com.mycompany.delsym.daos

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll

class CassandraDbDaoTest extends FunSuite
                         with BeforeAndAfterAll {

  val cassDao = new CassandraDbDao()
  
  override def afterAll() = cassDao.close()
  
  test("get non-existent record") {
    val url = "http://www.google.com"
    cassDao.deleteByUrl(url)
    val result = 
      cassDao.getByUrl(url, List.empty)
    result match {
      case Left(f) => fail("unexpected exception")
      case Right(r) => {
        Console.println("result=[" + r + "]")
        assert(r != null)
        assert(r.isEmpty)
      }
    }
  }
  
  test("insert after fetch") {
    val url = "http://www.google.com"
    val content = "<HTML>Some arbitary content</HTML>"
    val fetchMetadata = List(
       ("feed_title", "Some arbitary title"),
       ("feed_summary", "Jack the giant killer")).toMap
    cassDao.insertFetched(url, 0, fetchMetadata, content)
    cassDao.getByUrl(url, List.empty) match {
      case Right(row) => {
        Console.println("row (after fetch, #-cols:" + 
          row.size + ") = " + row)
        assert(row != null)
        assert(! row.isEmpty)
        assert(row.size == 9)
      }
      case Left(f) => fail("unexpected exception")
    }
  }
  
  test("insert after parse") {
    val url = "http://www.google.com"
    val textContent = "Some arbitary content"
    val parseMetadata = List(
       ("title", "The real title"),
       ("author", "Jack G Killer")).toMap
    cassDao.insertParsed(url, textContent, parseMetadata)
    cassDao.getByUrl(url, List.empty) match {
      case Right(row) => {
        Console.println("row (after parse, #-cols:" + 
          row.size + ") = " + row)
        assert(row != null)
        assert(! row.isEmpty)
        assert(row.size == 11)
      }
      case Left(f) => fail("unexpected exception")
    }
  }
  
  test("insert after index") {
    val url = "http://www.google.com"
    cassDao.insertIndexed(url)
    cassDao.getByUrl(url, List.empty) match {
      case Right(row) => {
        Console.println("row (after index, #-cols:" + 
          row.size + ") = " + row)
        assert(row != null)
        assert(! row.isEmpty)
        assert(row.size == 11)
      }
      case Left(f) => fail("unexpected exception")
    }
  }

  test("get inserted record, selected fields") {
    val url = "http://www.google.com"
    val result = cassDao.getByUrl(url, List("pts", "parsemeta"))
    result match {
      case Right(row) => {
        Console.println("partial row get = " + row)
        assert(row != null)
        assert(! row.isEmpty)
        assert(row.size == 3)
      }
      case Left(f) => {
        f.e.printStackTrace()
        fail("unexpected exception", f.e)
      }
    }
  }
}