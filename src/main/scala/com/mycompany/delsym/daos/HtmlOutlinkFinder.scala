package com.mycompany.delsym.daos

import java.util.regex.Pattern

class HtmlOutlinkFinder extends BaseOutlinkFinder {

  val OutlinkPattern = 
    Pattern.compile("""(https|http)\://\S+\.\S{2,3}(/\w*)?""")
    
  override def findOutlinks(url: String, 
      content: String): Either[FailResult,List[String]] = {
    try {
      val matcher = OutlinkPattern.matcher(content)
      val matches = Stream.continually(matcher.find())
        .takeWhile(m => m == true)
        .map(m => matcher.group())
        .toList
      Right(matches)
    } catch {
      case e: Exception => 
        Left(FailResult("Error finding outlinks", e))
    }
  }
}