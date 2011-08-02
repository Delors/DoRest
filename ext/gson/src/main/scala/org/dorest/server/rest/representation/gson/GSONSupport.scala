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
package org.dorest.server
package rest
package representation
package gson

import com.google.gson._
import java.io._
import java.nio.charset._
import io.Codec
import io.Source
import io.Codec._

/**
 * TODO write docu
 *
 * '''Remark''': As of Scala 2.9.0 the JSON library included with Scala is not really maintained and only handles a
 * subset of full JSON.
 */
trait GSONSupport {

    private val gson = new Gson()

    type DomainType <: Object
    def domainClass: java.lang.Class[DomainType]

    protected implicit def jsonObjectToSomeJSONObject(json: Object): Option[Object] = Some(json)

    /**
     * Generates a JSON representation for the JSONObject/JSONArray
     */
    def JSON(getJSONObject: ⇒ Option[Object]) =
        RepresentationFactory(MediaType.APPLICATION_JSON) {
        getJSONObject map { (o) => o match {
            case s : Seq[_] => {
            	val myArray = java.lang.reflect.Array.newInstance(domainClass,s.length).asInstanceOf[Array[Any]]
            	s.copyToArray(myArray)
                new UTF8BasedRepresentation(MediaType.APPLICATION_JSON, Codec.toUTF8(gson.toJson(myArray)))
            }
            case x =>  new UTF8BasedRepresentation(MediaType.APPLICATION_JSON, Codec.toUTF8(gson.toJson(x)))
        }}
     }

    private[this] var body: DomainType = _

    def JSON: RequestBodyProcessor = new RequestBodyProcessor(
        MediaType.APPLICATION_JSON,
        (charset: Option[Charset], in: InputStream) ⇒ {

            charset match {
                case Some(charset) ⇒
                    body = gson.fromJson((Source.fromInputStream(in)(Codec(charset)).mkString), domainClass)
                case _ ⇒
                    // HTTP 1.1 says that the default charset is ISO-8859-1
                    body = gson.fromJson((Source.fromInputStream(in)(Codec(Charset.forName("ISO-8859-1"))).mkString), domainClass)
            }
        }
    )

    def JSONRequestBody: DomainType =
        if (body == null)
            throw new Error("The request body's media type is not application/json.")
        else
            body

}