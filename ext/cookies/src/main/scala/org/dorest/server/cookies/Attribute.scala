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
class Attribute[T](val name: String) {
    var value: Option[T] = None
    def set(value: T) = {
        if(value !=null)
        	this.value = Option(value)
        else
        	this.value= None
    }

    def serialize = name + "=" + value.get
}

class ExtensionAttribute() extends Attribute[String](""){
    override def serialize = value.get
}
class DateAttribute(override val name: String) extends Attribute[Date](name) {
    override def serialize = name + "=" + formatDate(value.get)

    def formatDate(date: Date) = {
        val formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"))
        formatter.format(date)
    }
}

class FlagAttribute(override val name: String) extends Attribute[Boolean](name){
    override def set(value:Boolean) ={
        if(value)
            this.value=Some(value)
        else
            this.value=None
    }
    override def serialize = name
}