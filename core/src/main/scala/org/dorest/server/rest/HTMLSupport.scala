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

import io.Codec

/**
 * Use this trait to return a text based HTML representation of your resources.
 *
 * E.g., the code of a "Time" resource which offers a HTML based representation:
 * {{{
 * class Time
 *      extends RESTInterface
 *      with HTMLSupport {
 *
 *   get returns HTML {
 *      "<html><body>The current (server) time is: "+
 *          (new java.util.Date().toString)+
 *      "</body></html>"
 * }
 * }
 * }}}
 * @author Michael Eichberg
 */
trait HTMLSupport {

    protected implicit def charSequenceToSomeHtml(html: CharSequence): Option[CharSequence] = Some(html)

    def HTML(getHTML: ⇒ Option[CharSequence]) =
        RepresentationFactory(MediaType.HTML) {
            getHTML map ((html: CharSequence) ⇒ {
                new UTF8BasedRepresentation(MediaType.HTML, Codec.toUTF8(html))
            })
        }
}
