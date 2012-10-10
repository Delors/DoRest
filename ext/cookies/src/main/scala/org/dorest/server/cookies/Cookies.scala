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

import java.io.InputStream
import java.util.Date

import scala.collection.mutable.HashMap
import scala.collection.mutable.MultiMap

import org.dorest.server.Handler
import org.dorest.server.Response

class Cookie(val name: String, protected var _value:String="") {
    Ensure(name!=null,"The cookie name must not be null")
    Ensure(name.length>0,"The cookie name must not be empty")
    def value = _value
}

object CookieFactory {
    def createFrom(cookiesString: String): MultiMap[String, Cookie] = {
        var result = new HashMap[String, collection.mutable.Set[Cookie]] with MultiMap[String, Cookie]
        cookiesString.split(";").foreach(cookiePair => {
            val pair = cookiePair.split("=")
            val name = pair(0).trim
            val value = pair(1).trim

            result.addBinding(name, new Cookie(name, value))
        })
        result
    }
}

object Ensure{
    def apply(test:Boolean, msg:String)=if(!test) throw new IllegalArgumentException(msg)
}

class ResponseCookie(name:String) extends Cookie(name) {
    Ensure(containsOnlyCharsWithoutCTLs(name),"The cookie name must not contain controls")
    Ensure(!containsSeperators(name),"The cookie name must not contain seperators")
    
    
    
    def containsSeperators(testString:String)={
        val seperators="()<>@,;:\\\"/[]?={} "
        testString.forall(char=>            
            seperators.contains(char))
    }
    
    var _expires = new DateAttribute("Expires")
    var _maxAge = new Attribute[Int]("Max-Age")
    var _domain= new Attribute[String]("Domain")
    var _path=new Attribute[String]("Path");
    var _secure=new FlagAttribute("Secure");
    var _httpOnly=new FlagAttribute("HttpOnly");
    var _extension=new ExtensionAttribute

    val attributes = List(_expires, _maxAge, _domain, _path, _secure, _httpOnly, _extension)

    def value(newValue: String): ResponseCookie = {
        val testValue=if(startsAndEndsWithDoubleQuote(newValue))
            newValue.substring(1).substring(0, newValue.length-2)
        else
            newValue
            
        Ensure(!testValue.contains("\""),"The cookie value must not contain double quotes except if it starts and end with")
        Ensure(!testValue.contains(","),"The cookie value must not contain \",\"")
        Ensure(!testValue.contains(";"),"The cookie value must not contain \";\"")
        Ensure(!testValue.contains(" "),"The cookie value must not contain \" \"")
        Ensure(!testValue.contains("\\"),"The cookie value must not contain \"\\\"")
        Ensure(containsOnlyCharsWithoutCTLs(testValue),"The cookie value must not contain controls")
            
        this._value = newValue
        ResponseCookie.this
    }
    
    def startsAndEndsWithDoubleQuote(testString:String)={
        testString.length()>1 && (testString startsWith("\"")) && (testString endsWith("\""))
    }

    def maxAge(maxAge: Int) = {
        Ensure(maxAge>0,"The Max-Age attribute must be >0")
        _maxAge set maxAge
        ResponseCookie.this
    }

    def expires(expires: Date) = {
        _expires set expires
        ResponseCookie.this
    }

    def domain(domain: String) = {
        Ensure(DomainMatcher.isDomain(domain),domain + " is not a valid domain")
        _domain set domain
        this
    }
    
    def isControlCharacter(testChar:Char)=testChar<=0x1F || testChar==0x7F
    
    def containsOnlyCharsWithoutCTLs(testString:String)={
        testString.forall(char =>
                !isControlCharacter(char))
    }
    
    def path(path:String)={
        Ensure(containsOnlyCharsWithoutCTLs(path) && !path.contains(";"),"a path must not contain controls or ';'")
        _path set path
        this
    }
    
    def secure:ResponseCookie = secure(true);
    def secure(active:Boolean)={
        _secure set active
        this
    }

    def httpOnly:ResponseCookie = httpOnly(true);
    def httpOnly(active:Boolean)={
        _httpOnly set active
        this
    }
    
    def extension(extension:String)={
        Ensure(containsOnlyCharsWithoutCTLs(extension) && !extension.contains(";"),"the extension must not contain controls or ';'")
        _extension set extension
        this
    }
    
    override def toString = {
        var result = name + "=" + _value
        var stringAttributes = for (attribute <- attributes if !attribute.value.isEmpty) yield attribute.toString
        if (!stringAttributes.isEmpty)
            result += "; " + stringAttributes.reduceLeft(_ + "; " + _)
        result
    }
}

trait ResponseCookies {
    var responseCookies: collection.Map[String, ResponseCookie] = new HashMap

    object set {
        def cookie(name: String): ResponseCookie = {
            if (responseCookies.contains(name))
                responseCookies.get(name).get
            else {
                var cookie = new ResponseCookie(name)
                responseCookies = responseCookies.+((name, cookie))
                cookie
            }
        }
    }
}

trait Cookies extends ResponseCookies with Handler {

    lazy val requestCookies: MultiMap[String, Cookie] = {
        val cookiesString = requestHeaders.getFirst("Cookie")
        if (cookiesString == null) {
            new HashMap[String, collection.mutable.Set[Cookie]] with MultiMap[String, Cookie]
        } else {
            CookieFactory createFrom cookiesString
        }
    }

    def cookie(cookieName: String): collection.Set[Cookie] = {
        requestCookies.get(cookieName).getOrElse(new collection.mutable.HashSet[Cookie]())
    }

    def cookies = requestCookies.values

    override abstract def processRequest(requestBody: => InputStream): Response = {
        def serializeCookie(cookie: Cookie) = {
            cookie.name + "=" + cookie.value
        }

        val response: Response = super.processRequest(requestBody)

        responseCookies.foreach {
            case (name, cookie) =>
                response.headers.set("Set-Cookie", serializeCookie(cookie))
        }
        response
    }
}