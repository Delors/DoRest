/*
   Copyright 2012 Andreas Frankenberger

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
package org.dorest.server.cookies

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DomainMatcherTest extends FunSuite with ShouldMatchers {
    test("Domain with letters only") {
        assert(DomainMatcher.isDomain("A.ISI.EDU"))
        assert(DomainMatcher.isDomain("SRI-NIC.ARPA"))
    }

    test("A digit is no domain") {
        assert(!DomainMatcher.isDomain("1"))
    }

    test("\" \" is a domain") {
        assert(DomainMatcher.isDomain(" "))
    }

    test("abcDEF is a domain") {
        assert(DomainMatcher.isDomain("abcDEF"))
    }

    test("a-b is a domain") {
        assert(DomainMatcher.isDomain("a-b"))
    }

    test("a0 is a domain") {
        assert(DomainMatcher.isDomain("a0"))
    }

    test("- is no domain") {
        assert(!DomainMatcher.isDomain("-"))
    }

    test("a- is no domain") {
        assert(!DomainMatcher.isDomain("a-"))
    }

    test(". is no domain") {
        assert(!DomainMatcher.isDomain("."))
    }

}