/*
   Copyright 2011 Michael Eichberg et al

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.dorest.client

import org.apache.http.auth.params.AuthPNames
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.params.AuthPolicy
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.FileEntity
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.util
import java.nio.charset.Charset

/**
 * Simple HTTP-client for testing purposes.
 *
 * @author Mateusz Parzonka
 */
object SimpleClient {

  def get(headers: Map[String, String], auth: Auth = NoAuth)(url: String): Response = {
    execute(new HttpGet(url), headers, auth)
  }

  def post(headers: Map[String, String], auth: Auth = NoAuth)(url: String, entity: HttpEntity): Response = {
    executeWithEntity(new HttpPost(url), headers, auth, entity)
  }

  def put(headers: Map[String, String], auth: Auth = NoAuth)(url: String, entity: HttpEntity): Response = {
    executeWithEntity(new HttpPut(url), headers, auth, entity)
  }
  
  def delete(headers: Map[String, String], auth: Auth = NoAuth)(url: String): Response = {
    execute(new HttpDelete(url), headers, auth)
  }

  private[this] def executeWithEntity[T <: HttpEntityEnclosingRequestBase, S <: Auth, U <: HttpEntity](request: T, headers: Map[String, String], auth: S, entity: U): Response = {
    request.setEntity(entity)
    execute(request, headers, auth)
  }

  private[this] def execute[T <: HttpRequestBase, S <: Auth](request: T, headers: Map[String, String], auth: S): Response = {
    for ((key, value) <- headers) request.addHeader(key, value)
    val httpClient = auth.applyScheme(new DefaultHttpClient())
    new Response(httpClient.execute(request))
  }

}

object Entity {
  def apply(content: String) = new StringEntity(content)
  def apply(content: String, contentType: String, contentEncoding: String) = new StringEntity(content, contentType, contentEncoding)
  def apply(file: java.io.File, contentType: String, contentEncoding: String = null) = {
    val entity = new FileEntity(file, contentType)
    if (contentEncoding != null) entity.setContentEncoding(contentEncoding)
    entity
  }
  def apply(parts: (String, ContentBody)*): MultipartEntity = {
    val entity = new MultipartEntity()
    for (part <- parts) {
      entity.addPart(part._1, part._2)
    }
    entity
  }
}

object Part {
  def apply(content: String) = StringBody.create(content, "text/plain", null)
  def apply(content: String, mimeType: String) = {println("CONTENT: " + content); StringBody.create(content, mimeType, Charset.forName("UTF-8"))}
  def apply(file: java.io.File, contentType: String) = new FileBody(file, file.getName, contentType, "UTF-8")
}

/**
 * Wraps org.apache.http.HttpResponse with a basic interface.
 */
class Response(private val response: HttpResponse) {

  val statusCode: Int = response.getStatusLine().getStatusCode()

  val contentLength = if (response.getEntity() != null) response.getEntity().getContentLength() else null

  val contentType = response.getEntity() match {
    case entity: HttpEntity if entity.getContentType() != null => entity.getContentType().getValue().toString()
    case _ => null
  }

  lazy val bytes: Array[Byte] = {
    val entity = response.getEntity()
    val bytes = util.EntityUtils.toByteArray(entity)
    util.EntityUtils.consume(entity)
    bytes
  }

  def body: String = new String(bytes, "UTF-8")

}

abstract class Auth {

  def applyScheme(httpClient: DefaultHttpClient): DefaultHttpClient

  protected[this] def applyScheme(httpClient: DefaultHttpClient, creds: UsernamePasswordCredentials, authPolicy: String): DefaultHttpClient = {
    val credsProvider = new BasicCredentialsProvider()
    credsProvider.setCredentials(AuthScope.ANY, creds)
    httpClient.setCredentialsProvider(credsProvider)
    val targetPrefs = new java.util.ArrayList[String]()
    targetPrefs.add(authPolicy)
    httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, targetPrefs)
    httpClient
  }

}

object NoAuth extends Auth {

  def applyScheme(httpClient: DefaultHttpClient): DefaultHttpClient = httpClient

}

class BasicAuth(private val username: String, private val password: String) extends Auth {

  def applyScheme(httpClient: DefaultHttpClient): DefaultHttpClient =
    applyScheme(httpClient, new UsernamePasswordCredentials(username, password), AuthPolicy.BASIC)

}

class DigestAuth(username: String, password: String) extends Auth {

  def applyScheme(httpClient: DefaultHttpClient): DefaultHttpClient =
    applyScheme(httpClient, new UsernamePasswordCredentials(username, password), AuthPolicy.DIGEST)
}
