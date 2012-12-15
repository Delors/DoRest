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

import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


/**
 * Represents an attribute of a cookie as described in <a href="http://tools.ietf.org/html/rfc6265#section-5.2">the cookie rfc</a>
 * @author Andreas Frankenberger
 *
 * @param <T> The type of the attribute value
 */
class Attribute[T](val name: String) {
    var value: Option[T] = None
    /**
     * Sets the cookie value
     * @param value the actual attribute value
     */
    def set(value: T) = {
        if(value !=null)
        	this.value = Option(value)
        else
        	this.value= None
    }

    /**
     * @return A string representation of this attribute
     */
    def serialize = name + "=" + value.get
}

/**
 * Implementation for the <a href="http://tools.ietf.org/html/rfc6265#section-4.1.1">extension-attribute</a> which can be part of a cookie.
 * @author Andreas Frankenberger
 *
 */
class ExtensionAttribute() extends Attribute[String](""){
    override def serialize = value.get
}


/**
 * Implementation for the <a href="http://tools.ietf.org/html/rfc6265#section-5.2.1">expires attribute</a>, which has a date value that requires a special <a href="http://tools.ietf.org/html/rfc2616#section-3.3.1">date format</a>. 
 * @author Andreas Frankenberger
 *
 */
class DateAttribute(override val name: String) extends Attribute[Date](name) {
    override def serialize = name + "=" + formatDate(value.get)

    def formatDate(date: Date):String = {
        val formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"))
        formatter.format(date)
    }
}

/**
 * Implementation for attributes which are used as flag, without any value.
 * These are the <a href="http://tools.ietf.org/html/rfc6265#section-5.2.5">secure-attribute</a> and the <a href="http://tools.ietf.org/html/rfc6265#section-5.2.6">httponly-attribute</a>.
 * @author Andreas Frankenberger
 *
 */
class FlagAttribute(override val name: String) extends Attribute[Boolean](name){
    override def set(value:Boolean) ={
        if(value)
            this.value=Some(value)
        else
            this.value=None
    }
    override def serialize = name
}