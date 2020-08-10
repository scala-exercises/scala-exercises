/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.client
package common

import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

class ExtAjax(ajax: Ajax.type) {

  def postAsForm(
      url: String,
      data: String = "",
      timeout: Int = 0,
      headers: Map[String, String] = Map.empty,
      withCredentials: Boolean = false
  ) = {
    val contentType = ("Content-Type" -> "application/x-www-form-urlencoded")
    apply("POST", url, data, timeout, headers + contentType, withCredentials)
  }

  def postAsJson(
      url: String,
      data: String = "",
      timeout: Int = 0,
      headers: Map[String, String] = Map.empty,
      withCredentials: Boolean = false
  ) = {
    val contentType = ("Content-Type" -> "application/json")
    apply("POST", url, data, timeout, headers + contentType, withCredentials)
  }

  def apply(
      method: String,
      url: String,
      data: String = "",
      timeout: Int = 0,
      headers: Map[String, String] = Map.empty,
      withCredentials: Boolean = false
  ) = {
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
