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
 * @author Michael Eichberg
 */
@RunWith(classOf[JUnitRunner])
class RESTURIsMatcherTest extends FlatSpec with ShouldMatchers {

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

    // some URIMatcher instance
    val URIMatcher = new RESTURIsMatcher {}

    import URIMatcher._

    val exhaustiveMatcher =  / {
        case ""         ⇒ AHandler
        case "lectures" ⇒ BHandler
        case "users" ⇒ / {
            case MATCHED()   ⇒ CHandler
            case ROOT() ⇒ DHandler
            case LONG(userId) if userId > 0 ⇒ / {
                case EOL()      ⇒ LongHandler(userId)
                case ROOT()     ⇒ FHandler
                case "comments" ⇒ GHandler
            }

        }
    }

    "A RESTURIsMatcher" should "correctly match valid URIs" in {
        exhaustiveMatcher("/") should be (Some(AHandler))
        exhaustiveMatcher("/lectures") should be(Some(BHandler))
        exhaustiveMatcher("/users") should be(Some(CHandler))
        exhaustiveMatcher("/users/") should be(Some(DHandler))
        exhaustiveMatcher("/users/121212") should be(Some(LongHandler(121212)))
        exhaustiveMatcher("/users/23233321212/") should be(Some(FHandler))
        exhaustiveMatcher("/users/23233321212/comments") should be(Some(GHandler))
    }

    it should "handle URIs that do not match without throwing exceptions" in {
        exhaustiveMatcher("/INVALID") should be(None)
        exhaustiveMatcher("/lecturesall") should be(None)
        exhaustiveMatcher("/users/-121212") should be(None)
    }

    it should "not match URIs that are too long" in {
        exhaustiveMatcher("/lectures/") should be(None)
        exhaustiveMatcher("/lectures/slides") should be(None)
        exhaustiveMatcher("/users/23233321212/comments/2323") should be(None)
    }
}