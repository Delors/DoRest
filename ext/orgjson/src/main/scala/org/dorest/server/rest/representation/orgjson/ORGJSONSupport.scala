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
 * '''Remark:''' As of Scala 2.9.0 the JSON library included with Scala is not really maintained and only handles a
 * subset of full JSON.
 */
trait ORGJSONSupport {

    protected implicit def jsonObjectToSomeJSONObject (json : JSONObject) : Option[JSONObject] = Some(json)

    /**
     * Generates a JSON representation for the JSONObject.
     */
    def JSON(getJSONObject: => Option[JSONObject]) =
        RepresentationFactory(MediaType.JSON) {
            getJSONObject map ((json) => new UTF8BasedRepresentation(MediaType.JSON, Codec.toUTF8(json.toString)))
        }

    private[this] var body: JSONObject = _

    def JSON: RequestBodyProcessor = new RequestBodyProcessor(
        MediaType.JSON,
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