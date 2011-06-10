package org.dorest.server

object Utils {

    // TODO Consider using java.nio....use Charset.encode …=> nio.ByteBuffer
    final def toUTF8(s: String): Array[Byte] = s.getBytes(UTF8)

    val UTF8 = java.nio.charset.Charset.forName("UTF-8")
}