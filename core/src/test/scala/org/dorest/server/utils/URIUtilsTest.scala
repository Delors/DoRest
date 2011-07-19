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