package org.dorest.server.cookies

import org.dorest.server.Handler

class Cookie {
    protected var _value, _maxAge, _expires: String = _;
    def value = _value
}

class ResponseCookie extends Cookie {

    def value(newValue: String) = {
        this._value = newValue
        ResponseCookie.this
    }

    def maxAge(maxAge: String) = {
        _maxAge = maxAge
        ResponseCookie.this
    }
    
    def expires(expires: String) = {
        _expires = expires;
        ResponseCookie.this
    }
}

trait Cookies {
    self: Handler =>

    def setCookie( cookieName: String ) = { new ResponseCookie() }

    def cookie(cookieName: String): Option[Cookie] = {
        val cookie = requestHeaders.getFirst(cookieName)
        if (cookie == null || cookie.isEmpty)
            None
        else
            Some(new Cookie)
    }
}