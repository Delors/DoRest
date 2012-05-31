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
package org.dorest.server.utils

import java.nio.charset.Charset

/**
 * @author Michael Eichberg
 */
object URIUtils {

    def decodeRawURLQueryString(query: String): Option[Map[String, List[Option[String]]]] = {
        decodeRawURLQueryString(query, scala.io.Codec.UTF8)
    }

    /**
     * The implementation is based on the description given in:
     * <a href="http://tools.ietf.org/pdf/rfc3986.pdf">RFC 3986, URI Generic Syntax, January 2005</a>.
     *
     * @param query the raw query string which may use URL encoding.
     * @param charset the charset that has to be used to decode the string.
     * @todo check that all special cases (and in particular URLs that are tampered with) do not cause any unexpected behavior
     */
    def decodeRawURLQueryString(query: String, charset: Charset): Option[Map[String, List[Option[String]]]] = {
        if ((query eq null) || (query.length == 0))
            return None

        try {
            var param_values = Map[String, List[Option[String]]]().withDefaultValue(List[Option[String]]())
            for (param_value ← query.split('&')) {
                val index = param_value.indexOf('=')
                if (index == -1) {
                    val param = decodePercentEncodedString(param_value, charset)
                    param_values = param_values.updated(param, param_values(param) :+ None)
                } else {
                    val param = decodePercentEncodedString(param_value.substring(0, index), charset)
                    val value = decodePercentEncodedString(param_value.substring(index + 1), charset)
                    param_values = param_values.updated(param, param_values(param) :+ Some(value))
                }
            }
            Some(param_values)
        } catch {
            case _ ⇒ None
        }
    }

    protected[utils] def decodePercentEncodedString(encoded: String, charset: Charset): String = {
        java.net.URLDecoder.decode(encoded, charset.name)
    }
}

