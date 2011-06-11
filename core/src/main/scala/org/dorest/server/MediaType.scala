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

/**
 * List of all media types.
 */
object MediaType extends Enumeration {
    val any = Value("*/*")
    val XML = Value("application/xml")
    val JSON = Value("application/json")
    val TEXT = Value("text/plain")
    val HTML = Value("text/html")
    val CSS = Value("text/css")
    val JAVASCRIPT = Value("application/javascript") // or "text/javascript"
    val GIF = Value("image/gif")
    val JPEG = Value("image/jpeg")
    val PNG = Value("image/png") // Portable Network Graphics; Registered,[7] Defined in RFC 2083
    val SVG = Value("image/svg+xml") // SVG vector image; Defined in SVG Tiny 1.2 Specification Appendix M
    val TIFF = Value("image/tiff") //  Tag Image File Format (only for Baseline TIFF); Defined in RFC 3302
    val ATOM = Value("application/atom+xml") // Atom feeds
    val PDF = Value("application/pdf")
}