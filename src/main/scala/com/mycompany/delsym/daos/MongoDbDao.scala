package com.mycompany.delsym.daos

import java.util.Date
import scala.collection.JavaConversions.asScalaSet
import com.mongodb.casbah.Imports.MongoClient
import com.mongodb.casbah.Imports.MongoDBObject
import com.mongodb.casbah.Imports.wrapDBObj
import com.typesafe.config.ConfigFactory
import com.mongodb.casbah.WriteConcern

class MongoDbDao extends BaseDbDao {

  val conf = ConfigFactory.load()
  val mongoClient = MongoClient(
    conf.getString("delsym.mongodb.host"),
    conf.getInt("delsym.mongodb.port"))
  val dbname = conf.getString("delsym.mongodb.dbname")
  val collname = conf.getString("delsym.mongodb.collname")
  val mongoColl = mongoClient(dbname)(collname)
  
  override def insertFetched(url: String, depth: Int, 
      fetchMetadata: Map[String,Any],
      content: String): Either[FailResult,Unit] = {
    val query = MongoDBObject("url" -> url)
    val builder = MongoDBObject.newBuilder
    builder += "content" -> content
    builder += "url" -> url
    builder += "depth" -> depth
    fetchMetadata.foreach(
      kv => builder += "f_" + kv._1 -> kv._2)
    builder += "fts" -> new Date().getTime()
    try {
      mongoColl.update(query, builder.result, true, 
        false, WriteConcern.Safe)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error inserting fetch data", e))
    }
  }
  
  override def insertParsed(url: String, text: String, 
      parseMetadata: Map[String,Any]): 
      Either[FailResult,Unit] = {
    val query = MongoDBObject("url" -> url)
    val builder = MongoDBObject.newBuilder
    parseMetadata.foreach(
      kv => builder += "p_" + kv._1 -> kv._2)
    builder += "textContent" -> text
    builder += "pts" -> new Date().getTime()
    val update = MongoDBObject("$set" -> builder.result)
    try {
      mongoColl.update(query, update, true, 
        false, WriteConcern.Safe)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error inserting parse data", e))
    }
  }
  
  override def insertIndexed(url: String): 
      Either[FailResult,Unit] = {
    val query = MongoDBObject("url" -> url)
    val update = MongoDBObject("$set" -> MongoDBObject(
      "its" -> new Date().getTime()))
    try {
      mongoColl.update(query, update, true, 
        false, WriteConcern.Safe)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error inserting index data", e))
    }
  }
      
  override def getByUrl(url: String, fields: List[String]): 
      Either[FailResult,Map[String,Any]] = {
    try {
      val query = MongoDBObject("url" -> url)
      mongoColl.findOne(query) match {
        case Some(row) => {
          if (fields.isEmpty) {
            Right(Map() ++ row.keySet()
              .map(field => (field, row(field))))
          } else {
            Right(Map() ++ fields
              .map(field => (field, row(field))))
          }
        }
        case None => Right(Map.empty)
      }
    } catch {
      case e: Exception => Left(FailResult(e.getMessage(), e))
    }
  }
  
  override def close(): Unit = mongoClient.close()
}