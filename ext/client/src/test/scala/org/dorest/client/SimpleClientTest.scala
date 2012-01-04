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
package org.dorest.client

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Mateusz Parzonka
 */
@RunWith(classOf[JUnitRunner])
class RESTClientTest extends FlatSpec with ShouldMatchers {

  var get = SimpleClient.get(Map("ACCEPT" -> "text/html")) _

  "SimpleClient" should "GET http://www.google.com with status 200" in {
    get("http://www.google.com").statusCode should equal { 200 }
  }

  it should "GET http://www.google.com/thispagedoesnotexist with status 404" in {
    get("http://www.google.com/thispagedoesnotexist").statusCode should equal { 404 }
  }

}