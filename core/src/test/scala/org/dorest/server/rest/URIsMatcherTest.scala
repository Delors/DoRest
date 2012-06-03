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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/** Tests the matching of URIs.
  *
  * @author Michael Eichberg
  */
@RunWith(classOf[JUnitRunner])
class URIsMatcherTest extends FlatSpec with ShouldMatchers {

    // just some dummy handlers
    class DummyHandler extends Handler {
        def processRequest(requestBody: ⇒ java.io.InputStream): Response = {
            null
        }
    }
    object AHandler extends DummyHandler
    object BHandler extends DummyHandler
    object CHandler extends DummyHandler
    object DHandler extends DummyHandler
    object EHandler extends DummyHandler
    object FHandler extends DummyHandler
    object GHandler extends DummyHandler

    case class LongHandler(l: Long) extends DummyHandler
    case class PathHandler(p: String) extends DummyHandler

    // some URIMatcher instance
    val URIMatcher = new URIsMatcher {

        def register(handlerFactory: HandlerFactory) { throw new Error() }

    }

    import URIMatcher._

    val exhaustiveMatcher = / {
        case ""         ⇒ AHandler
        case "lectures" ⇒ BHandler
        case "users" ⇒ / {
            case MATCHED() ⇒ CHandler
            case ROOT()    ⇒ DHandler
            case LONG(userId) if userId > 0 ⇒ / {
                case MATCHED()  ⇒ LongHandler(userId)
                case ROOT()     ⇒ FHandler
                case "comments" ⇒ GHandler
            }
        }
        case "static" ⇒ (path) ⇒ Some((query) ⇒ Some(PathHandler(path)))
        case "sub"    ⇒ bind path (PathHandler)
    }

    "A RESTURIsMatcher" should "correctly match valid URIs" in {
        exhaustiveMatcher("/").get(null) should be(Some(AHandler))
        exhaustiveMatcher("/lectures").get(null) should be(Some(BHandler))
        exhaustiveMatcher("/users").get(null) should be(Some(CHandler))
        exhaustiveMatcher("/users/").get(null) should be(Some(DHandler))
        exhaustiveMatcher("/users/121212").get(null) should be(Some(LongHandler(121212)))
        exhaustiveMatcher("/users/23233321212/").get(null) should be(Some(FHandler))
        exhaustiveMatcher("/users/23233321212/comments").get(null) should be(Some(GHandler))
        exhaustiveMatcher("/static/").get(null) should be(Some(PathHandler("/")))
        exhaustiveMatcher("/static/index.html").get(null) should be(Some(PathHandler("/index.html")))
        exhaustiveMatcher("/static").get(null) should be(Some(PathHandler(null)))
        exhaustiveMatcher("/sub/index.html").get(null) should be(Some(PathHandler("/index.html")))
    }

    it should "handle URIs that do not match without throwing exceptions" in {
        exhaustiveMatcher("/INVALID") should be(None)
        exhaustiveMatcher("/lecturesall") should be(None)
        exhaustiveMatcher("/users/-121212") should be(None)
    }

    it should "not match URIs that are too long or too short" in {
        exhaustiveMatcher("/sub") should be(None)
        exhaustiveMatcher("/lectures/") should be(None)
        exhaustiveMatcher("/lectures/slides") should be(None)
        exhaustiveMatcher("/users/23233321212/comments/2323") should be(None)
    }
}