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

import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import org.junit.Test

class URIUtilsTest extends JUnitSuite {

    import URIUtils._

    @Test def testDecodePercentEncodedString() {
        assert( decodePercentEncodedString("a+b%20c",scala.io.Codec.UTF8) === "a b c" )

        intercept[IllegalArgumentException]{
        	decodePercentEncodedString("a+b%2",scala.io.Codec.UTF8)
        }

    }

    @Test def testdecodeRawURLQueryString() {

        assert( decodeRawURLQueryString("") === None )

        assert( decodeRawURLQueryString("foo") === Some(Map("foo" -> List(None))))
        assert( decodeRawURLQueryString("foo&bar") === Some(Map("foo" -> List(None), "bar" -> List(None))))

        assert( decodeRawURLQueryString("=foo") === Some(Map("" -> List(Some("foo")))))
        assert( decodeRawURLQueryString("=foo&=bar") === Some(Map("" -> List(Some("foo"),Some("bar")))))
        assert( decodeRawURLQueryString("=foo&=bar&=") === Some(Map("" -> List(Some("foo"),Some("bar"),Some("")))))

        assert( decodeRawURLQueryString("start=1&end=2&search=\"%20+++\"") === Some(Map("start" -> List(Some("1")),"end" -> List(Some("2")),"search" -> List(Some("\"    \"")))))
    }
}