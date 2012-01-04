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
package org.dorest.server.auth

import java.security.{ SecureRandom, MessageDigest }
import java.lang.{ StringBuilder => GoodSB }

object StringUtils extends StringUtils

/**
 * Some of the following code is based on utility methods from net.liftweb.util.{StringHelpers, SecurityHelpers}, developed as part of liftweb.net.
 * (Apache License V2.0 [http://www.apache.org/licenses/LICENSE-2.0],  Copyright 2006-2011 WorldWide Conferencing, LLC)
 *
 * @author Mateusz Parzonka
 */
trait StringUtils {

  private val random = new SecureRandom

  private val md = MessageDigest.getInstance("MD5");

  /**
   * Splits a string of the form name1=value1, name2=value2, ...  and unquotes the values.
   * @return a list of value pairs as List[String, String]
   */
  def splitNameValuePairs(props: String): List[(String, String)] = {
    val list = props.split(",").toList.map(in => {
      val pair = trimSplit(in, "=")
      (pair(0), unquote(pair(1)))
    })
    list
  }

  /**
   * Converts a list of tuples to a map when the mapping is injective or returns None in the other case.
   */
  def uniqueMap[A, B](s: Seq[(A, B)]) = {
    val m = s.toMap
    if (m.size == s.length) Some(m) else None
  }

  /**
   * If str is surrounded by quotes it returns the content between the quotes
   */
  def unquote(str: String) = {
      // TODO can we remove the null check?
    if (str != null && str.length >= 2 && str.charAt(0) == '\"' && str.charAt(str.length - 1) == '\"')
      str.substring(1, str.length - 1)
    else
      str
  }

  /**
   * Split a string according to a separator
   * @param sep a regexp to use with the String::split method
   * @return a list of trimmed parts whose length is &gt; 0
   */
  def trimSplit(what: String, sep: String): List[String] =
    what match {
      case null => Nil
      case s => s.split(sep).toList.map(_.trim).filter(_.length > 0)
    }

  /**
   * Create a random string of a given size.  5 bits of randomness per character
   * @param size size of the string to create. Must be a positive integer.
   * @return the generated string
   */
  def randomString(size: Int): String = {
    def addChar(pos: Int, lastRand: Int, sb: GoodSB): GoodSB = {
      if (pos >= size) sb
      else {
        val randNum = if ((pos % 6) == 0) {
          random.synchronized(random.nextInt)
        } else {
          lastRand
        }

        sb.append((randNum & 0x1f) match {
          case n if n < 26 => ('a' + n).toChar
          case n => ('0' + (n - 26)).toChar
        })
        addChar(pos + 1, randNum >> 5, sb)
      }
    }
    addChar(0, 0, new GoodSB(size)).toString
  }

  /**
   * Calculate MD5 digest
   */
  def md5(string: String) = md.digest(string.getBytes("UTF-8"))

  /**
   * Encode a Byte array as hexadecimal characters
   */
  def hexEncode(in: Array[Byte]): String = {
    val sb = new StringBuilder
    val len = in.length
    def addDigit(pos: Int) {
      if (pos < len) {
        val b: Int = in(pos)
        val msb = (b & 0xf0) >> 4
        val lsb = (b & 0x0f)
        sb.append((if (msb < 10) ('0' + msb).asInstanceOf[Char] else ('a' + (msb - 10)).asInstanceOf[Char]))
        sb.append((if (lsb < 10) ('0' + lsb).asInstanceOf[Char] else ('a' + (lsb - 10)).asInstanceOf[Char]))

        addDigit( pos + 1)
      }
    }
    addDigit(0)
    sb.toString
  }

  /**
   * Parses a string encoding a hex (without any prefix) and returns an int
   */

  def hexString2Int(str: String): Int = {

    def byteOf(in: Char): Int = in match {
      case '0' => 0
      case '1' => 1
      case '2' => 2
      case '3' => 3
      case '4' => 4
      case '5' => 5
      case '6' => 6
      case '7' => 7
      case '8' => 8
      case '9' => 9
      case 'a' | 'A' => 10
      case 'b' | 'B' => 11
      case 'c' | 'C' => 12
      case 'd' | 'D' => 13
      case 'e' | 'E' => 14
      case 'f' | 'F' => 15
      case _ => 0
    }

    val max = str.length - 1

    @scala.annotation.tailrec
    def loop(result: Int, pos: Int, mult: Int): Int = {
      if (pos <= max) {
        loop(result + byteOf(str.charAt(max - pos)) * mult, pos + 1, mult << 4)
      } else {
        result
      }
    }

    loop(0, 0, 1)
  }

}
