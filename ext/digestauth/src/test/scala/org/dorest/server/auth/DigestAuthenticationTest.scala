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

package org.dorest.server.auth

import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest._
import org.dorest.client._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.ShouldMatchers
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.{ ResponseHandler, HttpClient }
import org.apache.http.protocol.HttpContext
import org.apache.http.{ HttpRequest, HttpHost }
import scala.sys.SystemProperties
import org.dorest.server.HandlerFactory
import org.dorest.server.auth.StringUtils._

import scala.xml.{ XML, Utility }

/**
 * TestServer with unrestricted root and a restricted area.
 *
 * @author Mateusz Parzonka
 */
object DigestAuthTestServer extends JDKServer(9999) {

  this register new HandlerFactory[RootResource] {
    path { "/" }
    def create = new RootResource
  }

  this register new HandlerFactory[RestrictedResource] {
    path { "/restricted" }
    def create = new RestrictedResource
  }

  start()

  class RootResource extends RESTInterface with XMLSupport {

    get returns XML { <hello>"Hello!"</hello> }

  }

  class RestrictedResource extends RESTInterface with DigestAuthentication with DigestAuthenticatorMock with XMLSupport {

    get returns XML {<hello>{ "Hello " + authenticatedUser + "!" }</hello>}

  }

}

trait DigestAuthenticatorMock {

  def authenticationRealm = "http://www.somewhere.org"

  def password(username: String): Some[String] = Some("password")

}

/**
 * @author Mateusz Parzonka
 */
@RunWith(classOf[JUnitRunner])
class DigestAuthenticationTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll {

  override def beforeAll(configMap: Map[String, Any]) {
    println("Starting tests")
    DigestAuthTestServer
  }

  import org.apache.http._
  val get = SimpleClient.get(Map("ACCEPT" -> "application/xml")) _
  val authGet = SimpleClient.get(Map("ACCEPT" -> "application/xml"), new DigestAuth("somebody", "password")) _
  val falseAuthGet = SimpleClient.get(Map("ACCEPT" -> "application/xml"), new DigestAuth("somebody", "falsePassword")) _

  "RestrictedResource" should "return 401 for unauthorized (no credentials)" in {
    get("http://localhost:9999/restricted").statusCode should equal { 401 }
  }

   it should "return 401 for wrong credentials" in {
    falseAuthGet("http://localhost:9999/restricted").statusCode should equal { 401 }
  }

  it should "return 200 for authorized" in {
    authGet("http://localhost:9999/restricted").statusCode should equal { 200 }
  }

}

object MyApp extends scala.App {
  DigestAuthTestServer
}

