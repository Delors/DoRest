package org.dorest.server.cookies

import org.dorest.server.Handler

trait Cookies {
    self: Handler =>
        
    class Cookie{
        var _value: String;
        
        def value = _value
    }
    
    class ResponseCookie extends Cookie{

        def value(newValue: String): ResponseCookie = {
            _value = newValue
            ResponseCookie.this
        }
    }

    def setCookie(cookieName: String): ResponseCookie
    
    def cookie(cookieName: String): Option[Cookie] = {
        val cookie = requestHeaders.getFirst(cookieName)
        if (cookie == null || cookie.isEmpty)
            None
        else
            Some(new Cookie)
    }
}