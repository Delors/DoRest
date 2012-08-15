package org.dorest.server.cookies

import org.dorest.server.rest.RESTInterface
import org.dorest.server.rest.TEXTSupport

class Demo extends RESTInterface with Cookies with TEXTSupport{
    get returns TEXT {
        this setCookie "myCookie" value "tastes good" expires "tomorrow" maxAge "10 days";
        ""
    }
}