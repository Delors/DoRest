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
import org.scalatest.BeforeAndAfterEach

/**
 * Tests the [[org.dorest.server.ResponseHeaders]] trait.
 *
 * @author Michael Eichberg
 */
@RunWith(classOf[JUnitRunner])
class ResponseHeadersTest extends FlatSpec with ShouldMatchers with BeforeAndAfterEach {

    private var responseHeaders: DefaultResponseHeaders = _

    override def beforeEach {
        responseHeaders = DefaultResponseHeaders()
    }

    "setAcceptRanges" should "set the Accept-Ranges response header" in {
        responseHeaders.setAcceptRanges("bytes")("accept-ranges") should be (Set("bytes"))
    }
    
    it should "not defined fields twice" in {
        responseHeaders.setAcceptRanges("bytes1")
        responseHeaders.setAcceptRanges("bytes2")("accept-ranges") should be (Set("bytes2"))
    }

    "setAge" should "set the Age response header" in {
        responseHeaders.setAge(1000l)("age") should be (Set("1000"))
        responseHeaders.setAge(2000l)("age") should be (Set("2000"))
    }

    "setAllow" should "set the Allow response header" in {
        responseHeaders.setAllow(GET,OPTIONS)("allow") should be (Set("GET, OPTIONS"))
        responseHeaders.setAllow(POST,OPTIONS)("allow") should be (Set("POST, OPTIONS"))
    }

    "setContentLanguage" should "set the Content-Language response header" in {
        import java.util.Locale._
        responseHeaders.setContentLanguage(GERMAN,UK)("content-language") should be (Set("de, en-GB"))
        responseHeaders.setContentLanguage(UK)("content-language") should be (Set("en-GB"))
    }
}