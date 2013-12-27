package com.mycompany.delsym.daos

import java.util.concurrent.atomic.AtomicLong

trait BaseDao {}

abstract class BaseHttpFetcher extends BaseDao {
  def fetch(url: String): Either[FailResult,String]
}

abstract class BaseDbDao extends BaseDao {
  def insertFetched(url: String, depth: Int, 
    fetchMetadata: Map[String,Any], 
    content: String): Either[FailResult,Unit]
  
  def insertParsed(url: String, text: String, 
    parseMetadata: Map[String,Any]): 
    Either[FailResult,Unit]
    
  def insertIndexed(url: String): Either[FailResult,Unit]
  
  def getByUrl(url: String, fields: List[String]): 
    Either[FailResult,Map[String,Any]]
  
  def close(): Unit
}

abstract class BaseParser extends BaseDao {
  def parse(url: String, content: String): 
    Either[FailResult,Pair[String,Map[String,Any]]]
}

abstract class BaseOutlinkFinder extends BaseDao {
  def findOutlinks(url: String): 
    Either[FailResult,List[(String,Int,Map[String,Any])]]
}

abstract class BaseSolrPublisher extends BaseDao {
  def publish(url: String, row: Map[String,Any]):
    Either[FailResult,Unit]
  
  def commit(): Unit
  
  def close(): Unit
}

case class FailResult(msg: String, e: Exception)

////////////////// Mock DAOs for unit testing //////////////

object MockCounters {
  val fetched = new AtomicLong(0L)
  val parsed = new AtomicLong(0L)
  val indexed = new AtomicLong(0L)
  val dbFetched = new AtomicLong(0L)
  val dbParsed = new AtomicLong(0L)
  val dbIndexed = new AtomicLong(0L)
  val outlinkCalled = new AtomicLong(0L)
}

class MockHttpFetcher extends BaseHttpFetcher { 
  def fetch(url: String): Either[FailResult,String] = {
    MockCounters.fetched.incrementAndGet()
    Right(null)
  }
}

class MockDbDao extends BaseDbDao {
  def insertFetched(url: String, depth: Int, 
      fetchMetadata: Map[String,Any], 
      content: String): Either[FailResult,Unit] = {
    MockCounters.dbFetched.incrementAndGet()
    Right()
  }
  
  def insertParsed(url: String, text: String, 
      parseMetadata: Map[String,Any]): 
      Either[FailResult,Unit] = {
    MockCounters.dbParsed.incrementAndGet()
    Right()
  }
    
  def insertIndexed(url: String): 
      Either[FailResult,Unit] = {
    MockCounters.dbIndexed.incrementAndGet()
    Right()
  }
  
  def getByUrl(url: String, fields: List[String]): 
      Either[FailResult,Map[String,Any]] = {
    Right(Map("url" -> url, "content" -> "test"))
  }
  
  def close(): Unit = {}
}

class MockParser extends BaseParser { 
  def parse(url: String, content: String): 
      Either[FailResult,Pair[String,Map[String,Any]]] = {
    MockCounters.parsed.incrementAndGet()
    Right("test", Map("url" -> url, "content" -> "test"))
  }
}

class MockOutlinkFinder extends BaseOutlinkFinder { 
  def findOutlinks(url: String): 
      Either[FailResult,List[(String,Int,Map[String,Any])]] = {
    MockCounters.outlinkCalled.incrementAndGet()
    Right(List.empty)
  }
}

class MockSolrPublisher extends BaseSolrPublisher { 
  def publish(url: String, row: Map[String,Any]):
    Either[FailResult,Unit] = {
    MockCounters.indexed.incrementAndGet()
    Right()
  }
  
  def commit(): Unit = {}
  
  def close(): Unit = {}
}

////////////////////// end Mock DAOs //////////////////////
