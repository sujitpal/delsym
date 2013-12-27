package com.mycompany.delsym.daos

import java.util.regex.Pattern
import com.typesafe.config.ConfigFactory

class HtmlOutlinkFinder extends BaseOutlinkFinder {

  val OutlinkPattern = 
    Pattern.compile("""(https|http)\://\S+\.\S{2,3}(/\w*)?""")
  
  val conf = ConfigFactory.load()
  val testUser = conf.getBoolean("delsym.testuser")
  val mongoDbDao = if (testUser) new MockDbDao()
                   else new MongoDbDao()
                   
  override def findOutlinks(url: String):
      Either[FailResult,List[(String,Int,Map[String,Any])]] = {
    try {
      mongoDbDao.getByUrl(url, List.empty) match {
        case Right(row) => { 
          if (row.contains("content")) { 
            val content = row("content").asInstanceOf[String]
            val depth = row.getOrElse("depth", 0)
                           .asInstanceOf[Int]
            val fetchMeta = row.keys
              .filter(k => ! k.startsWith("f_"))
              .map(k => (k, row(k)))
              .toMap
            if (depth > 0) {
              val matcher = OutlinkPattern.matcher(content)
              val matches = Stream.continually(matcher.find())
                .takeWhile(m => m == true)
                .map(m => matcher.group())
                .toList
              Right(matches.map(m => 
                (m, depth - 1, fetchMeta)))
            } else {
              Right(List.empty)
            }
          } else {
            Right(List.empty)
          }
        }
        case _ => Right(List.empty)
      }
    } catch {
      case e: Exception => 
        Left(FailResult("Error finding outlinks", e))
    }
  }
}