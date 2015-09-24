package common

import upickle._

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax
import shared._

class ExtAjax(ajax: Ajax.type) {

  def postAsForm(url: String,
           data: String = "",
           timeout: Int = 0,
           headers: Map[String, String] = Map.empty,
           withCredentials: Boolean = false) = {
    val contentType = ("Content-Type" -> "application/x-www-form-urlencoded")
    apply("POST", url, data, timeout, headers + contentType, withCredentials)
  }

  def postAsJson(url: String,
                 data: String = "",
                 timeout: Int = 0,
                 headers: Map[String, String] = Map.empty,
                 withCredentials: Boolean = false) = {
    val contentType = ("Content-Type" -> "application/json")
    apply("POST", url, data, timeout, headers + contentType, withCredentials)
  }

  def apply(method: String,
            url: String,
            data: String = "",
            timeout: Int = 0,
            headers: Map[String, String] = Map.empty,
            withCredentials: Boolean = false) = {
    val ajaxReq = ("X-Requested-With" -> "XMLHttpRequest")
    ajax.apply(method, url, data, timeout, headers + ajaxReq, withCredentials, "")
  }

}

class ExtXMLHttpRequest(req: XMLHttpRequest) {

//  def responseAs[T](implicit readWrite: ReadWriter[T]): T = read[T](req.responseText)

  def ok = req.status == 200

}

object ExtAjax {

  implicit def wrapperForAjax(ajax: Ajax.type) = new ExtAjax(ajax)

  implicit def wrapperForXMLHttpRequest(req: XMLHttpRequest) = new ExtXMLHttpRequest(req)

}