package com.mycompany.delsym.daos

import java.io.ByteArrayInputStream

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.parser.audio.AudioParser
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.image.ImageParser
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.opendocument.OpenOfficeParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.rtf.RTFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.WriteOutContentHandler

class TikaParser extends BaseParser {

  override def parse(url: String, content: String): 
      Either[FailResult,Pair[String,Map[String,Any]]] = {
    try {
      val handler = new WriteOutContentHandler(-1)
      val metadata = new Metadata()
      val ctx = new ParseContext()
      val parser = getParser(url)
      parser.parse(new ByteArrayInputStream(
        content.getBytes), handler, metadata, ctx)
      val parseMetadata = List(
        ("title", metadata.get(Metadata.TITLE)),
        ("author", metadata.get(Metadata.CREATOR)))
        .filter(x => x._2 != null)
        .toMap
      Right(Pair(handler.toString(), parseMetadata))      
    } catch {
      case e: Exception => Left(FailResult(
        "Parsing of URL:" + url + " failed", e))
    }
  }
  
  
  def getParser(url: String): Parser = {
    val suffix = url.slice(
      url.lastIndexOf("."), url.length())
    suffix match {
      case "text" | "txt" => new TXTParser()
      case "html" | "htm" => new HtmlParser()
      case "xml"          => new XMLParser()
      case "pdf"          => new PDFParser()
      case "rtf"          => new RTFParser()
      case "odt"          => new OpenOfficeParser()
      case "xls" | "xlsx" => new OfficeParser()
      case "doc" | "docx" => new OfficeParser()
      case "ppt" | "pptx" => new OfficeParser()
      case "pst"          => new OfficeParser()
      case "vsd"          => new OfficeParser()
      case "png"          => new ImageParser()
      case "jpg" | "jpeg" => new ImageParser()
      case "mp3"          => new AudioParser()
      case _              => new AutoDetectParser()
    }
  }
}