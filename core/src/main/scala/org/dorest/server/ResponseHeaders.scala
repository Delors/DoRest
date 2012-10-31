/*
   Copyright 2011, 2012 Michael Eichberg et al

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

import scala.collection.Traversable

/**
  * An HTTP response's headers.
  *
  * For an overview go to [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14 HTTP Header Fields]].
  *
  * @author Michael Eichberg
  */
trait ResponseHeaders extends Traversable[(String, String)] {

    /**
      * Adds the value (value) of the specified response/entity header (field).
      *
      * If this method is directly used, it is
      * the responsibility of the caller to make sure that the value is valid.
      * Cf. [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14 HTTP Header Fields]] for further
      * details. In general, it is recommended to use one of the predefined methods to set an entity/a response
      * header field.
      *
      * @note Though, names of request and response header fields are case-insensitive
      * (cf. [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2 HTTP/1.1 Notational Conventions]]),
      * they are typically used using small letters and DoRest follows this convention.
      *
      * @param field The name of the response header; e.g., "age".
      * @param value The value of the response header; e.g., "10021" in case of the "age" response header field.
      */
    def add(field: String, value: String): this.type
    
    /**
     * Removes a field from the header.
     * @param field The name of the field that will be removed from the header
     */
    protected def remove(field:String):this.type

    /**
      * Sets the Accept-Ranges header field.
      *
      * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.5 RFC 2616 - Accept-Ranges]]
      */
    def setAcceptRanges(acceptableRanges: String): this.type = {
        require(acceptableRanges ne null)
        require(acceptableRanges.length > 0)

        remove("accept-ranges")
        add("accept-ranges", acceptableRanges)
        this
    }

    /**
      * Sets the Age header field.
      *
      * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.6 RFC 2616 - Age]]
      */
    def setAge(timeInSecs: Long): this.type = {
        require(timeInSecs >= 0)

        remove("age")
        add("age", String.valueOf(timeInSecs))
        this
    }

    /**
      * Sets the Allow header field.
      *
      * @see [[org.dorest.core.ResponseHeaders.setAllow(Seq[HTTPMethod])]]
      */
    def setAllow(methods: HTTPMethod*): this.type = setAllow(methods.toSeq)

    /**
      * Sets the Allow header field.
      *
      * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.7 RFC 2616 - Allow]]
      * @note  An Allow header field MUST be present in a 405 (Method Not Allowed) response.
      * @param methods The non-empty set of supported HTTP methods.
      */
    def setAllow(methods: Traversable[HTTPMethod]): this.type = {
        require(methods.toSet.size == methods.size)

        remove("allow")
        add("allow", methods.mkString(", "))
        this
    }

    /**
      * Sets the Content-Language header field.
      *
      * @see [[org.dorest.core.ResponseHeaders.setContentLanguage(Seq[java.util.Locale])]]
      */
    def setContentLanguage(languageTags: java.util.Locale*): this.type = setContentLanguage(languageTags.toSeq)

    /**
      * Sets the content language header field.
      *
      * @see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.12 RFC 2616 - Content-Language]]
      * @note Language tags are case-insensitive. However, the ISO 639/ISO 3166 convention is that language
      *  names are written in lower case, while country codes are written in upper case (e.g., "en-US").
      *
      *  Other language tags, such as, "x-pig-latin" (cf.
      *  [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.10 RFC 2616 - Language Tags]]) could
      *  theoretically also be specified, but are not supported by this method. If you need to specify
      *  such a language tag, use the generic `set(field,value)` method.
      * @param languageTags A non-empty list of locales for which the language tag MUST be set.
      */
    def setContentLanguage(languageTags: Traversable[java.util.Locale]): this.type = {
        require(languageTags.size > 0)

        remove("content-language")
        add(
            "content-language",
            (localeToLanguageTag(languageTags.head) /: languageTags.tail)(_ + (", ") + localeToLanguageTag(_))
        )
        this
    }

    /**
      * Converts a locale object with a valid, non-empty language part into a language tag as defined by
      * [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.10 RFC 2616 - Language Tags]].
      *
      * @return A valid language tag.
      */
    def localeToLanguageTag(locale: java.util.Locale): String = {
        require(locale.getLanguage.length > 0)

        var languageTag = locale.getLanguage
        val country = locale.getCountry
        if (country.length > 0) {
            languageTag += "-" + country
        }
        languageTag
    }

    // TODO implement support for the rest of the HTTP/1.1 response/entity header fields

    /**
      * Calls the given function `f` for each header field/value pair.
      *
      * '''Typical Usage''': This method is used by DoRest to iterate over the set of specified response
      * headers.
      */
    def foreach[U](f: ((String, String)) ⇒ U): Unit
}





























