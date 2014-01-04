package com.mycompany.delsym.daos

import java.text.SimpleDateFormat
import java.util.Date

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.mutable.ArrayBuffer

import com.datastax.driver.core.Cluster
import com.typesafe.config.ConfigFactory

class CassandraDbDao extends BaseDbDao {

  val conf = ConfigFactory.load()
  val host = conf.getString("delsym.cassandradb.host")
  val dbname = conf.getString("delsym.cassandradb.dbname")
  val tablename = conf.getString("delsym.cassandradb.collname")
  
  val cluster = Cluster.builder()
                       .addContactPoint(host)
                       .build()
  val session = cluster.connect()
  session.execute("USE %s".format(dbname))
  
  override def insertFetched(url: String, depth: Int, 
      fetchMetadata: Map[String,Any], 
      content: String): Either[FailResult,Unit] = {
    try {
      deleteByUrl(url) match {
        case Left(f) => Left(f)
        case _ => {}
      }
      val fmeta = fetchMetadata.map(kv => 
          "'%s':'%s'".format(kv._1, kv._2.toString))
        .mkString(", ")
      val insfetch = "insert into documents(" + 
        "url, depth, content, fetchmeta, fts) values (" +
        "'%s', %d, '%s', {%s}, '%s');"
        .format(url, depth, content, fmeta, iso8609(new Date()))
      session.execute(insfetch)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error inserting fetch data", e))
    }
  }
  
  override def insertParsed(url: String, text: String, 
      parseMetadata: Map[String,Any]): 
      Either[FailResult,Unit] = {
    try {
      val pmeta = parseMetadata.map(kv => 
        "'%s':'%s'".format(kv._1, kv._2.toString))
        .mkString(", ")
      val insparse = "insert into documents(" +
        "url, text_content, parsemeta, pts) values (" +
        "'%s', '%s', {%s}, '%s');"
        .format(url, text, pmeta, iso8609(new Date()))
      session.execute(insparse)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error inserting parse data", e))
    }
  }
  
  override def insertIndexed(url: String): 
      Either[FailResult,Unit] = {
    try {
      val insindex = "insert into documents(url, its) " +
        "values('%s', '%s')"
        .format(url, iso8609(new Date()))
      session.execute(insindex)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error inserting index data", e))
    }
  }
  
  override def getByUrl(url: String, fields: List[String]):
      Either[FailResult,Map[String,Any]] = {
    try {
      val fldlist = if (fields.isEmpty) '*' 
                    else fields.mkString(",") 
      val query = "select %s from %s where url = '%s'"
        .format(fldlist, tablename, url) 
      val results = session.execute(query)
      val row = results.one()
      val colnames = if (fields.isEmpty)
        row.getColumnDefinitions()
          .asList()
          .map(coldef => coldef.getName())
          .toList
      else fields
      var colvals = ArrayBuffer[(String,Any)]()
      colnames.map(colname => colname match {
        case "url" => 
          colvals += (("url", row.getString("url")))
        case "content" => 
          colvals += (("content", row.getString("content")))
        case "depth" => 
          colvals += (("depth", row.getInt("depth")))
        case "fetchmeta" => {
          val fmeta = row.getMap("fetchmeta", 
            classOf[String], classOf[String])
          fmeta.map(kv => 
            colvals += (("f_" + kv._1, kv._2)))
        }
        case "fts" => 
          colvals += (("fts", row.getDate("fts")))
        case "text_content" => 
          colvals += (("textContent", row.getString("text_content")))
        case "parsemeta" => {
          val pmeta = row.getMap("parsemeta", 
            classOf[String], classOf[String])
          pmeta.map(kv => 
            colvals += (("p_" + kv._1, kv._2)))
        }
        case "pts" => 
          colvals += (("pts", row.getDate("pts")))
        case "its" => 
          colvals += (("its", row.getDate("its")))
        case _ => {}
      })
      Right(colvals.toMap)
    } catch {
      case e: NullPointerException => Right(Map.empty) 
      case e: Exception =>  
        Left(FailResult(e.getMessage(), e))
    }
  }
  
  override def close(): Unit = cluster.shutdown()

  def deleteByUrl(url: String): Either[FailResult,Unit] = {
    try {
      val delquery = 
        "delete from documents where url = '%s';"
        .format(url)
      session.execute(delquery)
      Right()
    } catch {
      case e: Exception => 
        Left(FailResult("Error deleting by URL: [" + 
          url + "]", e))
    }
  }
  
  def iso8609(d: Date): String = {
    lazy val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    sdf.format(d)
  }
}