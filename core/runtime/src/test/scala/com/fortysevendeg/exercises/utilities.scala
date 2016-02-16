/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package com.fortysevendeg.exercises

import scala.collection.JavaConversions._

import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.ClassLoader
import java.net.URL
import java.net.URLStreamHandler
import java.net.URLConnection
import java.nio.charset.StandardCharsets
import java.util.Enumeration

object RawDataResourceClassLoader {
  def apply(resources: (String, List[String])*): RawDataResourceClassLoader = RawDataResourceClassLoader(Map(resources: _*))
}

case class RawDataResourceClassLoader(
    resources: Map[String, List[String]],
    parent:    ClassLoader               = classOf[RawDataResourceClassLoader].getClassLoader
) extends ClassLoader(parent) {

  private final val URL_PROTOCOL = "47"
  private final val URL_HOST = "*"
  private final val URL_PORT = 1988

  private class RawDataURLConnection(url: URL, data: String) extends URLConnection(url) {
    override def connect() {}
    override def getInputStream() = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))
  }

  private class RawDataURLStreamHandler(name: String, data: String) extends URLStreamHandler {
    override def openConnection(url: URL) = (url.getProtocol, url.getHost, url.getPort, url.getFile) match {
      case (`URL_PROTOCOL`, `URL_HOST`, `URL_PORT`, `name`) ⇒ new RawDataURLConnection(url, data)
      case _ ⇒ throw new IOException(s"invalid URL ${url}")
    }
  }

  private def createRawDataURL(name: String, data: String) =
    new URL(URL_PROTOCOL, URL_HOST, URL_PORT, name, new RawDataURLStreamHandler(name, data))

  override def getResources(name: String): Enumeration[URL] =
    resources.getOrElse(name, Nil)
      .map(data ⇒ createRawDataURL(name, data))
      .iterator
}
