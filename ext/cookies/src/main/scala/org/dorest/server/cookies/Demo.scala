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

import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.TEXTSupport
import org.dorest.server.jdk.JDKServer
import org.dorest.server.rest.XMLSupport


object Demo extends JDKServer(9000) with App {

    addURIMatcher(
        / {
            case "cookieecho" ⇒ new CookieEcho()
            case "setcookies" ⇒ new SetCookies()
        }
    )

    start()
}
class CookieEcho extends RESTInterface with Cookies with XMLSupport{
    get returns XML {
        <received_cookies>
        	{ for (myCookies <- cookies) for(myCookie <- myCookies) yield 
            <cookie>
        		<name>{myCookie.name}</name>
        		<value>{myCookie.value}</value>
        	</cookie>}
        </received_cookies>
    }
}


class SetCookies extends RESTInterface with Cookies with XMLSupport{
    post of XML returns XML {
        (XMLRequestBody \\ "cookie") foreach { cookie =>
            val cookieName=(cookie\"name").text
            val cookieValue=(cookie\"value").text
            set cookie cookieName value cookieValue
        }
        <ok/>
    }
}