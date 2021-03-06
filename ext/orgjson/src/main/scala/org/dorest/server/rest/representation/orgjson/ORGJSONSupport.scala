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
package orgjson

import org.json._
import java.io._
import java.nio.charset._
import io.Codec
import io.Source
import io.Codec._

/**
 * Adds support for parsing and generating JSON representations using the <a href="http://www.json.org/java/">org.json
 * </a> library.
 *
 * '''Remark''': The org.json library does not define a common supertype for representing JSON objects and JSON arrays.
 * This required that we had to resort to use java.lang.Object as the common supertype which may cause conflicts with
 * other libraries.
 * '''Remark''': As of Scala 2.9.0 the JSON library included with Scala is not really maintained and only handles a
 * subset of full JSON.
 */
trait ORGJSONSupport {

    protected implicit def jsonObjectToSomeJSONObject(json: Object): Option[Object] = Some(json)

    /**
     * Generates a JSON representation for the JSONObject/JSONArray
     */
    def JSON(getJSONObject: => Option[Object]) =
        RepresentationFactory(MediaType.APPLICATION_JSON) {
            getJSONObject map ((json) => new UTF8BasedRepresentation(MediaType.APPLICATION_JSON, Codec.toUTF8(json.toString)))
        }


    private[this] var body: JSONObject = _

    def JSON: RequestBodyProcessor = new RequestBodyProcessor(
        MediaType.APPLICATION_JSON,
        (charset: Option[Charset], in: InputStream) => {
            charset match {
                case Some(charset) =>
                    body = new JSONObject(Source.fromInputStream(in)(Codec(charset)).mkString)
                case _ =>
                    // HTTP 1.1 says that the default charset is ISO-8859-1
                    body = new JSONObject(Source.fromInputStream(in)(Codec(Charset.forName("ISO-8859-1"))).mkString)
            }
        }
    )

    def JSONRequestBody: JSONObject =
        if (body == null)
            throw new Error("The request body's media type is not application/json.")
        else
            body

}