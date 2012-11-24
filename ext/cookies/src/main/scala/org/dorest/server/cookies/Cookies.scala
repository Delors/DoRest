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

/**
 * Represents a cookie send to a web server according to <a href="http://tools.ietf.org/html/rfc6265">rfc6265</a>.
 * 
 * @author Andreas Frankenberger
 */
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

/**
 * Represents a cookie servers send to clients according to <a href="http://tools.ietf.org/html/rfc6265">rfc6265</a>.
 * @author Andreas Frankenberger
 */
class ResponseCookie(name:String) extends Cookie(name) {
    Ensure(containsOnlyCharsWithoutCTLs(name),"The cookie name must not contain controls")
    Ensure(!containsSeperators(name),"The cookie name must not contain seperators")
    
    
    
    protected def containsSeperators(testString:String)={
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

    /**
     * Sets the value of a cookie. The value must not contain any control characters,  "',;\" or blanks.
     * The value may start and end with double quotes, but must not contain double quotes in between.
     */
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
    
    protected def startsAndEndsWithDoubleQuote(testString:String)={
        testString.length()>1 && (testString startsWith("\"")) && (testString endsWith("\""))
    }

    /**
     * Sets the maximum age in seconds of the cookie. It must be a value >0.
     */
    def maxAge(maxAge: Int) = {
        Ensure(maxAge>0,"The Max-Age attribute must be >0")
        _maxAge set maxAge
        ResponseCookie.this
    }

    /**
     * Sets the expire date of the cookie.
     */
    def expires(expires: Date) = {
        _expires set expires
        ResponseCookie.this
    }

    /**
     * Sets the domain attribute of the cookie. The domain must only contain the characters a-z, A-Z, 0-9, "-" and "/".
     */
    def domain(domain: String) = {
        Ensure(DomainMatcher.isDomain(domain),domain + " is not a valid domain")
        _domain set domain
        this
    }
    
    protected def isControlCharacter(testChar:Char)=testChar<=0x1F || testChar==0x7F
    
    protected def containsOnlyCharsWithoutCTLs(testString:String)={
        testString.forall(char =>
                !isControlCharacter(char))
    }
    
    /**
     * Sets the path attribute of the cookie. 
     */
    def path(path:String)={
        Ensure(containsOnlyCharsWithoutCTLs(path) && !path.contains(";"),"A path must not contain controls or ';'")
        _path set path
        this
    }
    
    /**
     * Limits the cookie to secure channels.
     */
    def secure:ResponseCookie = secure(true);
    
    /**
     * Limits the cookie to secure channels.
     * @param active true activates this attribute, false deactivates it.
     */
    def secure(active:Boolean)={
        _secure set active
        this
    }

    /**
     * Limits the scope of the cookie to http requests only.
     */
    def httpOnly:ResponseCookie = httpOnly(true);
    
    /**
     * Limits the scope of the cookie to http requests only.
     * @param active true activates this attribute, false deactivates it.
     */
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

/**
 * This trait is used to create response cookies.
 * 
 * @author Andreas Frankenberger
 */
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

/**
 * Adds cookie support to REST Handlers.
 * This trait supports setting response cookies and accessing request cookies.
 * 
 * @author Andreas Frankenberger
 */
trait Cookies extends ResponseCookies with Handler {

    private lazy val requestCookies: MultiMap[String, Cookie] = {
        val cookiesString = requestHeaders.getFirst("Cookie")
        if (cookiesString == null) {
            new HashMap[String, collection.mutable.Set[Cookie]] with MultiMap[String, Cookie]
        } else {
            CookieFactory createFrom cookiesString
        }
    }

    /**
     * Returns a collection of cookies with the given name.
     * @param cookieName the name of the cookies
     * @return a collection of cookies with the given name or an empty list if no one can be found.
     */
    def cookie(cookieName: String): collection.Set[Cookie] = {
        requestCookies.get(cookieName).getOrElse(new collection.mutable.HashSet[Cookie]())
    }

    def cookies = requestCookies.values

    override abstract def processRequest(requestBody: => InputStream): Response = {
        def serializeCookie(cookie: Cookie) = {
            cookie.toString()
        }

        val response: Response = super.processRequest(requestBody)

        responseCookies.foreach {
            case (name, cookie) =>
                response.headers.add("Set-Cookie", serializeCookie(cookie))
        }
        response
    }
}