/*
   Copyright 2012 Michael Eichberg et al

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

/**
 * Facilitates the matching of URIs to resources
 *
 * @author Michael Eichberg
 */
trait RESTURIsMatcher {

    // NEEDS TO BE PROVIDED BY THE CLASS WHERE THIS CLASS IS MIXED IN
    //    def register(handlerFactory: HandlerFactory[Handler])

    /**
     * Use ROOT to match a URI that ends with "/" and where all previous segments have been successfully
     * matched.
     */
    object ROOT {
        def unapply(s: String): Boolean = {
            !(s eq null) && s.length == 0
        }
    }

    /**
     * Use EOL to match a URI that does not end with "/".
     */
    object EOL {
        def unapply(s: String): Boolean = {
            s == null
        }
    }
    val MATCHED = EOL

    /**
     * Use String to match a path segment that is non-empty.
     */
    object STRING {
        def unapply(pathSegment: String): Option[String] = {
            if (!(pathSegment eq null) && pathSegment.length > 0)
                Some(pathSegment)
            else
                None
        }
    }

    object LONG {
        def unapply(pathSegment: String): Option[Long] = {
            if (pathSegment eq null) return None

            try {
                Some(java.lang.Long.parseLong(pathSegment, 10))
            } catch {
                case e: NumberFormatException ⇒ None
                case e                        ⇒ throw e;
            }
        }
    }

    type PathMatcher = (String) ⇒ Option[Handler]

    case class /(matcher: PartialFunction[String, PathMatcher]) extends PathMatcher {

        /**
         * @param completePath A valid URI path (or the yet unmatched part of the URI).
         *		The completePath is either null or is a string that starts with a "/".
         * 		The semantics of null is that the complete path was matched; i.e., there
         * 		is no remaining part.
         */
        def apply(completePath: String): Option[Handler] = {
            if (completePath == null)
                return {
                    if (matcher.isDefinedAt(null))
                        matcher(null)(null)
                    else
                        None // the provided path was too short; this is usually a design problem
                }

            if (completePath.charAt(0) != '/')
                throw new IllegalArgumentException("The provided path: \"" + completePath + "\" is invalid; it must start with a /.")

            val path = completePath.substring(1) // we truncate the trailing "/"
            val separatorIndex = path.indexOf('/')
            val head = if (separatorIndex == -1) path else path.substring(0, separatorIndex)
            val tail = if (separatorIndex == -1 || (separatorIndex == 0 && path.length == 1)) null else path.substring(separatorIndex)
            if (matcher.isDefinedAt(head))
                matcher(head)(tail)
            else
                None

        }
    }

    implicit def HandlerToPathMatcher(h: Handler): PathMatcher =
        (pathSegment: String) ⇒ if (pathSegment == null) Some(h) else None

    implicit def matcherToPS(matcher: PartialFunction[String, PathMatcher]): / = {
        /(matcher)
    }

    //    def addPath(pathMatcher: PathMatcher) {
    //
    //    }

    //    this addPath (
    //        / {
    //            case ROOT() ⇒ new Handler {}
    //            case "lectures" ⇒ / {
    //                case EOL()  ⇒ new Handler {} /* General information about lectures */
    //                case ROOT() ⇒ new Handler {} /* The lectures */
    //                case STRING(lectureId) ⇒ / {
    //                    //case NIL        ⇒ null /* General Information about the lecture */
    //                    case "slides" ⇒ new Handler {}
    //                }
    //            }
    //            case "users" ⇒ new Handler {}
    //        }
    //    )
}




