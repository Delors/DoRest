package org.dorest.server.cookies

import org.dorest.server.Handler
import scala.collection.immutable.HashMap
import scala.collection.JavaConverters._
import scala.collection.breakOut
import java.io.InputStream
import org.dorest.server.Response

class Cookie(val name:String, protected var _value:String) {
    def value = _value
}

object CookieFactory{
    def createFrom(cookiesString:String):Map[String,Cookie]={
        cookiesString.split(";").map ( { case cookiePair =>
            val pair=cookiePair.split("=")
            val name=pair(0).trim
            val value=pair(1).trim
            name -> new Cookie(name,value)
        })(breakOut)
    }
}

class ResponseCookie(name:String) extends Cookie(name,"") {
	protected var _name, _maxAge, _expires: String = _;
    
    def value(newValue: String) : ResponseCookie= {
        this._value = newValue
        ResponseCookie.this
    }    

    def maxAge(maxAge: String) = {
        _maxAge = maxAge
        ResponseCookie.this
    }
    
    def expires(expires: String) = {
        _expires = expires;
        ResponseCookie.this
    }
}

trait Dummy

trait Cookies extends Dummy with Handler{
    var responseCookies:Map[String,ResponseCookie]=new HashMap
    
    lazy val requestCookies:Map[String,Cookie]={
        val cookiesString=requestHeaders.getFirst("Cookie")
        if(cookiesString==null){
            new HashMap
        }else{
            CookieFactory createFrom cookiesString
        }
    }
    

    def cookie(cookieName: String): Option[Cookie] = {
        requestCookies.get(cookieName)
    }
    
    def cookies = requestCookies.values
    
    object set{
        def cookie(name:String) : ResponseCookie = {
	        if(responseCookies.contains(name))
	        	responseCookies.get(name).get
	        else{
	            var cookie = new ResponseCookie(name)
	        	responseCookies=responseCookies.+( (name , cookie))
	        	cookie
	        }
        }
    }
    
    override abstract def processRequest(requestBody: => InputStream): Response = {
        def serializeCookie(cookie:Cookie)={
            cookie.name+"="+cookie.value
        }
        
        val response:Response=super.processRequest(requestBody)
        
        responseCookies.foreach{case (name,cookie) => 
            response.headers.set("Set-Cookie", serializeCookie(cookie))
        }
        response
    }
}