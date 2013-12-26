package com.mycompany.delsym.daos

import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite

class SolrPublisherTest extends FunSuite
                        with BeforeAndAfterAll {

  val solrPublisher = new SolrPublisher()
  
  override def afterAll() = solrPublisher.close()
  
  test("publish index results to solr") {
    val url = "http://www.google.com"
    val row = (List(
      ("_id", "52b9dd3f67754069be0ac9bd"),
      ("content", "Some arbitary content"),
      ("depth", 0),
      ("f_feed_summary", "Jack the giant killer"),
      ("f_feed_title", "Some arbitary title"),
      ("fts", 1388082031374L),
      ("its", 1388082031413L),
      ("p_author", "Some arbitary_author"),
      ("p_title", "Some arbitary title"),
      ("pts", 1388082031411L),
      ("textContent", "Text of the page"),
      ("url", "http://www.google.com")))
      .toMap
    val result = solrPublisher.publish(url, row) match {
      case Left(f) => f.msg
      case _ => "OK"
    }
    solrPublisher.commit()
    assert(result == "OK")
  }
}