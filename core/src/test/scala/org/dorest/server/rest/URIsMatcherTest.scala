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

/**
 * Tests the matching of URIs.
 *
 * @author Michael Eichberg
 */
@RunWith(classOf[JUnitRunner])
class URIsMatcherTest extends FlatSpec with ShouldMatchers {

    // just some dummy handlers
    class DummyHandler extends Handler {
        def processRequest(requestBody: ⇒ java.io.InputStream): Response = null
    }
    object AHandler extends DummyHandler
    object BHandler extends DummyHandler
    object CHandler extends DummyHandler
    object DHandler extends DummyHandler
    object EHandler extends DummyHandler
    object FHandler extends DummyHandler
    object GHandler extends DummyHandler

    case class LongHandler(l: Long) extends DummyHandler
    case class StringHandler(p: String) extends DummyHandler
    case class OptionalLongHandler(l: Option[Long]) extends DummyHandler
    case class MultiHandler(l: Option[Long], s: String) extends DummyHandler

    // some URIMatcher instance
    val URIsMatcher = new URIsMatcher with DoRestApp {

//        var factories = List[HandlerFactory]()
//
//        def register(handlerFactory: HandlerFactory) { factories = factories :+ handlerFactory }

    }

    import URIsMatcher._

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
        case "static" ⇒ (path) ⇒ Some((query) ⇒ Some(StringHandler(path)))
        case "sub"    ⇒ bind path (StringHandler) // same as above
    }

    "A PathMatcher" should "correctly match valid URIs" in {
        exhaustiveMatcher("/").get(null) should be(Some(AHandler))
        exhaustiveMatcher("/lectures").get(null) should be(Some(BHandler))
        exhaustiveMatcher("/users").get(null) should be(Some(CHandler))
        exhaustiveMatcher("/users/").get(null) should be(Some(DHandler))
        exhaustiveMatcher("/users/121212").get(null) should be(Some(LongHandler(121212)))
        exhaustiveMatcher("/users/23233321212/").get(null) should be(Some(FHandler))
        exhaustiveMatcher("/users/23233321212/comments").get(null) should be(Some(GHandler))
        exhaustiveMatcher("/static/").get(null) should be(Some(StringHandler("/")))
        exhaustiveMatcher("/static/index.html").get(null) should be(Some(StringHandler("/index.html")))
        exhaustiveMatcher("/static").get(null) should be(Some(StringHandler(null)))
        exhaustiveMatcher("/sub/index.html").get(null) should be(Some(StringHandler("/index.html")))
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

    val SID = QueryMatcher(STRING("id"))
    val LID = QueryMatcher(LONG("id"))
    val MULTI = QueryMatcher(LONG("id"), STRING("search"))

    /*
     * Intended Semantics:
     * If I match a query string, I want to specify which parameters are required and which are optional (default)
     * If all required parameters can be matched, the query matcher succeeds.
     */
    val matcherWithQueryStrings = / {
        case "lectures" ⇒ ? { case SID(Some(id)) ⇒ StringHandler(id) }
        case "slides"   ⇒ ? { case LID(id) ⇒ OptionalLongHandler(id) }
        case "users"    ⇒ ? { case MULTI(id, Some(search)) ⇒ MultiHandler(id, search) }
    }

    "A QueryMatcher" should "correctly extract a single required query parameter" in {
        matcherWithQueryStrings("/lectures").get("id=Yes") should be(Some(StringHandler("Yes")))
        matcherWithQueryStrings("/lectures").get("foo=3434&id=2323") should be(Some(StringHandler("2323")))
        matcherWithQueryStrings("/lectures").get("") should be(None)
        matcherWithQueryStrings("/lectures").get(null) should be(None)
    }

    it should "correctly extract an optional query parameter" in {
        matcherWithQueryStrings("/slides").get("") should be(Some(OptionalLongHandler(None)))
        matcherWithQueryStrings("/slides").get(null) should be(Some(OptionalLongHandler(None)))
        matcherWithQueryStrings("/slides").get("id=sdfsdf") should be(Some(OptionalLongHandler(None)))
        matcherWithQueryStrings("/slides").get("id=121") should be(Some(OptionalLongHandler(Some(121l))))
        matcherWithQueryStrings("/slides").get("id=absct&id=121") should be(Some(OptionalLongHandler(Some(121l))))
    }

    it should "correctly extract many query parameters" in {
        matcherWithQueryStrings("/users").get("") should be(None)
        matcherWithQueryStrings("/users").get(null) should be(None)
        matcherWithQueryStrings("/users").get("search=sdfsdf") should be(Some(MultiHandler(None, "sdfsdf")))
        matcherWithQueryStrings("/users").get("id=121") should be(None)
        matcherWithQueryStrings("/users").get("id=121&search=abc") should be(Some(MultiHandler(Some(121), "abc")))
    }

}