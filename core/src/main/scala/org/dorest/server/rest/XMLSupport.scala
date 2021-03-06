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

import java.io._
import io.Codec
import scala.xml._
import java.nio.charset.Charset

/**
 * Provides support for handling XML representations.
 *
 * @author Michael Eichberg
 */
trait XMLSupport {

    protected implicit def xmlNodeToSomeXmlNode(node: Node) = Some(node)
    private[this] val prettyPrinter =  new scala.xml.PrettyPrinter(200,2)

    def XML(makeXML: ⇒ Option[Node]) = RepresentationFactory(MediaType.APPLICATION_XML) {
        makeXML map (xml ⇒ new Representation[MediaType.APPLICATION_XML.type] {

        	val response = Codec.toUTF8(prettyPrinter.format(xml))

            def contentType = Some((MediaType.APPLICATION_XML, Some(Codec.UTF8)))

            def length = response.length

            def write(out: OutputStream) {
                out.write(response)
            }
        })
    }

    // TODO scala.xml.Node or scala.xml.Elem?
    private[this] var body: Elem = _

    def XML: RequestBodyProcessor = new RequestBodyProcessor(
        MediaType.APPLICATION_XML,
        (charset: Option[Charset], in: InputStream) ⇒ {
            charset match {
                case Some(definedCharset) ⇒
                    body = scala.xml.XML.loadString(scala.io.Source.fromInputStream(in)(scala.io.Codec(definedCharset)).mkString)
                case _ ⇒
                    body = scala.xml.XML.loadString(scala.io.Source.fromInputStream(in)(Codec(Charset.forName("ISO-8859-1"))).mkString)
            }
        }
    )

    /**
     * Returns the request's body as an scala.xml.Elem object. 
     */
    def XMLRequestBody: Elem =
        if (body == null)
            throw new Error("The request body's media type is not application/xml.")
        else
            body
}