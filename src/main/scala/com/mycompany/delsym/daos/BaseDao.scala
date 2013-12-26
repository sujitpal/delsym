package com.mycompany.delsym.daos

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
  def findOutlinks(url: String, content: String): 
    Either[FailResult,List[String]]  
}

abstract class BaseSolrPublisher extends BaseDao {
  def publish(url: String, row: Map[String,Any]):
    Either[FailResult,Unit]
  
  def commit(): Unit
  
  def close(): Unit
}

case class FailResult(msg: String, e: Exception)

