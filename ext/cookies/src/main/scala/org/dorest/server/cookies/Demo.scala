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
        	{ for (cookie <- cookies) yield
            <cookie>
        		<name>{cookie.name}</name>
        		<value>{cookie.value}</value>
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