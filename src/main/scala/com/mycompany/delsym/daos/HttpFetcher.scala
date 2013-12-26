package com.mycompany.delsym.daos

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.HttpException
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.io.Source

class HttpFetcher extends BaseHttpFetcher {
  
  val conf = ConfigFactory.load()
  val numRetries = conf.getInt("delsym.fetchers.numRetries")

  override def fetch(url: String): Either[FailResult,String] = {
    val httpclient = new HttpClient()
    val getmethod = new GetMethod(url)
    getmethod.getParams().setParameter(
      HttpMethodParams.RETRY_HANDLER, 
      new DefaultHttpMethodRetryHandler(numRetries, false))
    try {
      val status = httpclient.executeMethod(getmethod)
      if (status == HttpStatus.SC_OK) {
        val is = getmethod.getResponseBodyAsStream()
        Right(Source.fromInputStream(is).mkString)
      } else {
        val message = "Fetch of %s failed (code=%d): %s"
          .format(url, status, getmethod.getStatusLine())
        Left(FailResult(message, null))
      } 
    } catch {
      case e: HttpException => 
        Left(FailResult("Fetch of [%s] failed, protocol violation"
        .format(url), e)) 
      case e: IOException => 
        Left(FailResult("Fetch of {} failed, transport error"
        .format(url), e))
    } finally {
      getmethod.releaseConnection()
    }
  }
}

