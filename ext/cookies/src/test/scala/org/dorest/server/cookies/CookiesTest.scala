/*
   Copyright 2012 Andreas Frankenberger

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
package org.dorest.server.cookies

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest._
import org.dorest.client._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterAll

object DigestAuthTestServer extends JDKServer(9999) {

    addURIMatcher(
        / {
            case "echo" => / {
                case STRING(cookieName) => new Echo(cookieName)
            }
            case "bigcookie" => new RESTInterface with Cookies with TEXTSupport{
                
            }
            case "threecookies" => new RESTInterface with Cookies with TEXTSupport{
                get returns TEXT {
                    set cookie "name1" value "value1"
                    set cookie "name2" value "value2"
                    set cookie "name3" value "value3"
                    ""
                }
            }
            case "doublenamed" => new RESTInterface with Cookies with TEXTSupport{
                get returns TEXT {
                    set cookie "name" value "value1"
                    set cookie "name" value "value2"
                    ""
                }
            }
            
            case "encoded" => new RESTInterface with Cookies with TEXTSupport{
                get returns TEXT {
                    set cookie "name" value "a b"
                    ""
                }
            }
        }
    )

    start()
}

class Echo(val echo:String) extends RESTInterface with Cookies with TEXTSupport {
	get returns TEXT { 
	    var result=""
	    cookie(echo).foreach(cookie =>{
	        result+=cookie.value+";"
	    })
	    result
	}
}


@RunWith(classOf[JUnitRunner])
class CookiesTest extends FlatSpec with ShouldMatchers with BeforeAndAfterAll{

    override def beforeAll(configMap: Map[String, Any]) {
        println("Starting tests")
        DigestAuthTestServer
    }
    
    val echoUrl="http://localhost:9999/echo"
    
    "echo/name" should "return \"works\"" in {
        SimpleClient.get(Map("Cookie" -> "name=works"))(echoUrl+"/name").body should equal { "works;" }
    }
    
    it should "return c1;c2 or c2;c1;" in {
        val responseBody= SimpleClient.get(Map("Cookie" -> "name=c1 ; name=c2"))(echoUrl+"/name").body
        responseBody should include ("c1;")
        responseBody should include ("c2;")
    }
    
    it should "decode values" in {
        SimpleClient.get(Map("Cookie" -> "name=a%20b"))(echoUrl+"/name").body should equal { "a b;" }
        SimpleClient.get(Map("Cookie" -> "name=a+b"))(echoUrl+"/name").body should equal { "a b;" }
    }
        
    "doublenamed" should "return Set-Cookie: name=value2" in {
        val headers= SimpleClient.get(Map())("http://localhost:9999/doublenamed").headers("Set-Cookie")
        headers should have length(1)
        headers(0) should be ("name=value2")
    }
    
    "threecookies" should "return three Set-Cookie headers" in { 
        val headers= SimpleClient.get(Map())("http://localhost:9999/threecookies").headers("Set-Cookie")
        headers should have length(3)
    }
    
    "encoded" should "encode \"a b\" to a+b" in{
        val headers= SimpleClient.get(Map())("http://localhost:9999/encoded").headers("Set-Cookie")
        headers should have length(1)
        headers(0) should be ("name=a+b")
    }
}