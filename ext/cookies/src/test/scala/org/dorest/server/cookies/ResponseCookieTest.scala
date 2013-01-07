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
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.BeforeAndAfter
import java.util.GregorianCalendar
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@RunWith(classOf[JUnitRunner])
class ResponseCookieTest extends FlatSpec with ShouldMatchers with BeforeAndAfter {

    var cookie: ResponseCookie = _

    before {
        cookie = new ResponseCookie("name");
    }

    "The cookie" should "be a name-value pair" in {
        cookie.value("value");
        cookie.serialize should include("name=value")
    }
    
    it should "encode its value" in {
        cookie.value("a b")
        cookie.serialize should include("name=a+b")
        
        cookie.value("\"value\"");
        cookie.serialize should include("name=%22value%22")
        cookie.value("\"ab\"c\"");
        cookie.serialize should include("name=%22ab%22c%22")
        
        cookie.value(";")
        cookie.serialize should include("name=%3B")
        cookie.value("\\")
        cookie.serialize should include("name=%5C")
        cookie.value(",")
        cookie.serialize should include("name=%2C")
        
        cookie.value(0x7F.toChar.toString)
        cookie.serialize should include("name=%7F")
    }

    it should "expire at Sun, 06 Nov 1994 08:49:37 GMT" in {
        cookie.expires(createDefaultDate)
        cookie.serialize should include("; Expires=Sun, 06 Nov 1994 08:49:37 GMT")
        cookie.expires(null)
        cookie.serialize should not include("; Expires=Sun, 06 Nov 1994 08:49:37 GMT")
    }
    
    it should "have a max age of 1000 seconds" in {
        cookie.maxAge(1000)
        cookie.serialize should include ("; Max-Age=1000"); 
    }
    
    it should "have the domain XX.LCS.MIT.EDU" in {
        cookie.domain("XX.LCS.MIT.EDU")
        cookie.serialize should include("; Domain=XX.LCS.MIT.EDU")
    }
    
    it should "have a path scope set to /the/path" in {
        cookie.path("/the/path")
        cookie.serialize should include("; Path=/the/path");
    }
    
     it should "throw exceptions if the path contains controls or ';'" in {
        intercept[IllegalArgumentException]{
            cookie.path(";")
        }
        intercept[IllegalArgumentException]{
            cookie.path(0x7F.toChar.toString);
        }
        intercept[IllegalArgumentException]{
            cookie.path(0x1F.toChar.toString);
        }
    }
    
    it should "be secure" in {
        cookie.secure
        cookie.serialize should include("; Secure");
        cookie.secure(false) 
        cookie.serialize should not include("; Secure")
        cookie.secure(true)
        cookie.serialize should include("; Secure");
    }
    
    it should "be HttpOnly" in {
        cookie.httpOnly
        cookie.serialize should include("; HttpOnly");
    }
    
    it should "have an extension string" in {
        cookie.extension("my extension")
        cookie.serialize should include("; my extension");
    }
    
    it should "throw exceptions if the extension contains controls or ';'" in {
        intercept[IllegalArgumentException]{
            cookie.extension(";")
        }
        intercept[IllegalArgumentException]{
            cookie.extension(0x7F.toChar.toString);
        }
        intercept[IllegalArgumentException]{
            cookie.extension(0x1F.toChar.toString);
        }
    }
    
    "The cookie name" should "not contain controls" in {
        testName(0x7F.toChar.toString)
        testName(0x1F.toChar.toString)
    }
    
    it should "not contain seperators such as '()<>@,;:\\\"/[]?={} '" in {
        testName("(")
        testName(")")
        testName("<")
        testName(">")
        testName("@")
        testName(",")
        testName(";")
        testName(":")
        testName("\\")
        testName("\"")
        testName("/")
        testName("[")
        testName("]")
        testName("?")
        testName("=")
        testName("{")
        testName("}")
        testName(" ")
    }
    
   def testName(name:String)={
       intercept[IllegalArgumentException]{
            new ResponseCookie(name);
        }
   }

    def createDefaultDate() = {
        val hour = 8
        val minute = 49
        val second = 37
        val cal = new GregorianCalendar(1994, Calendar.NOVEMBER, 6, hour, minute, second)
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.getTime
    }
}
