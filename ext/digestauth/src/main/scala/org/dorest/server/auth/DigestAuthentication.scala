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
package org.dorest.server
package auth

import java.io.InputStream
import StringUtils._

/**
 * Implementation of Digest Access Authentication (RFC 2617).
 *
 * @author Mateusz Parzonka
 */
trait DigestAuthentication extends Authentication with Handler {

  private[this] var _authenticatedUser: Option[String] = None

  def authenticatedUser = _authenticatedUser

  override abstract def processRequest(requestBody: InputStream): Response = {
    requestHeaders
    incomingRequest match {
      case authorizationRequest: AuthorizationRequest => validate(authorizationRequest) match {
        case ValidatedRequest => { _authenticatedUser = Some(authorizationRequest.username); super.processRequest(requestBody) }
        case StaleRequest => unauthorizedDigestResponse(stale = true)
        case _ => unauthorizedDigestResponse(stale = false)
      }
      case _ => unauthorizedDigestResponse(stale = false)
    }
  }

  def incomingRequest: Request = {
    requestHeaders.getFirst("Authorization") match {
      case authorizationHeader: String if authorizationHeader.startsWith("Digest ") => {
        parseAuthorizationHeader(authorizationHeader) match {
          case Some(m: Map[_,_]) => AuthorizationRequest(HTTPMethod.unapply(method), m("username"), m("realm"), m("nonce"), m("uri"), m("qop"), m("nc"), m("cnonce"), m("response"), m("opaque"))
          case _ => UnauthorizedRequest
        }
      }
      case _ => UnauthorizedRequest
    }
  }

  def parseAuthorizationHeader(authorizationHeader: String) = uniqueMap(splitNameValuePairs(authorizationHeader.substring("Digest ".length)))

  def unauthorizedDigestResponse(stale: Boolean): Response = {
    val nonce = randomString(64)
    NonceStorage.addNonce(nonce)
    UnauthorizedDigestResponse(authenticationRealm, "auth", nonce, randomString(64), stale)
  }

  def validate(r: AuthorizationRequest): Request = {
    password(r.username) match {
      case Some(pwd: String) => {
        val ha1 = hexEncode(md5(r.username + ":" + r.realm + ":" + pwd))
        val ha2 = hexEncode(md5(r.method + ":" + r.uri))
        val response = hexEncode(md5(ha1 + ":" + r.nonce + ":" + r.nc + ":" + r.cnonce + ":" + r.qop + ":" + ha2))
        (response == r.response) match {
          case true if (NonceStorage.contains(r.nonce, r.nc)) => ValidatedRequest
          case true => StaleRequest
          case _ => UnauthorizedRequest
        }
      }
      case _ => UnauthorizedRequest
    }
  }
}

/**
 * Thread-safe nonce-storage with background-deletion of expired nonces.
 *
 * @author Mateusz Parzonka
 */
object NonceStorage {

  import scala.collection.JavaConversions._
  private[this] val nonceMap: scala.collection.mutable.ConcurrentMap[String, (Int, Long)] = new java.util.concurrent.ConcurrentHashMap[String, (Int, Long)](32, 0.75f, 8)

  private def nonceValidityPeriod = 30000

  private def nonceCleaningInterval = 5000

  def addNonce(nonce: String) {
    nonceMap += (nonce -> (0, System.currentTimeMillis))
  }

  /**
   * Checks if the storage contains the given nonce assuring the given nc was not used before.
   */
  def contains(nonce: String, nc: String) = {
    val curNc = hexString2Int(nc)
    nonceMap.get(nonce) match {
      case Some((oldNc: Int, time: Long)) if curNc > oldNc => { nonceMap.replace(nonce, (curNc, time)); true }
      case _ => false
    }
  }

  def clean() {
    val currentTime = System.currentTimeMillis
    for ((nonce, (nc, time)) <- nonceMap if (currentTime - time) > nonceValidityPeriod) nonceMap.-=(nonce)
  }

  import scala.actors.{ Actor, TIMEOUT }
  import Actor._
  val nonceCleaner = actor {
    loop {
      reactWithin(nonceCleaningInterval) {
        case TIMEOUT => clean
      }
    }
  }

}

sealed abstract class Request
case object UnauthorizedRequest extends Request
case class AuthorizationRequest(method: String, username: String, realm: String, nonce: String, uri: String, qop: String, nc: String, cnonce: String, response: String, opaque: String) extends Request
case object ValidatedRequest extends Request
case object StaleRequest extends Request

