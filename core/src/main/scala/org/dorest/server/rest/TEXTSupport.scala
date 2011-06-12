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
 * Provides support for handling TEXT (based) representations.
 *
 * @author Michael Eichberg
 */
trait TEXTSupport {

    protected implicit def textToSomeText(html: String) : Option[String] = Some(html)

    def TEXT(getText: => Option[String]) =
        RepresentationFactory(MediaType.TEXT) {
            getText map ((text) => new UTF8BasedRepresentation(MediaType.TEXT, Codec.toUTF8(text)))
        }
}