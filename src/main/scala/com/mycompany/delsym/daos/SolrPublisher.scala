package com.mycompany.delsym.daos

import java.util.concurrent.atomic.AtomicLong

import org.apache.solr.client.solrj.impl.HttpSolrServer
import org.apache.solr.common.SolrInputDocument

import com.typesafe.config.ConfigFactory

class SolrPublisher extends BaseSolrPublisher {

  val conf = ConfigFactory.load()

  val solrServer = new HttpSolrServer(
    conf.getString("delsym.solr.server"))
  val dbFieldNames = conf.getString(
    "delsym.solr.dbfieldnames").split(",")
  val solrFieldNames = conf.getString(
    "delsym.solr.solrfieldnames").split(",")
  val fieldNameMap = solrFieldNames.zip(dbFieldNames).toMap
  val commitInterval = conf.getInt("delsym.solr.commitInterval")

  val numIndexed = new AtomicLong(0L)

  override def publish(url: String, row: Map[String,Any]):
      Either[FailResult,Unit] = {
    try {
      val doc = new SolrInputDocument()
      solrFieldNames
        .filter(f => row.contains(fieldNameMap(f)))
        .foreach(f =>
          doc.addField(f, row(fieldNameMap(f))
             .asInstanceOf[String]))
      solrServer.add(doc)
      if (numIndexed.incrementAndGet() % 
          commitInterval == 0) commit()
      Right()
    } catch {
      case e: Exception => Right(
        FailResult("Error publishing to Solr", e))
    }   
  }
  
  override def commit(): Unit = solrServer.commit()
  
  override def close(): Unit = solrServer.shutdown()
}