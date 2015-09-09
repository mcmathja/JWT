package io.chapbook.util.fmt

import org.apache.commons.codec.binary.Base64

/**
 * Wrappers for Base64 functions.
 */

object B64 {
  def encode(str: String) =
    Base64.encodeBase64URLSafeString(str.getBytes("utf-8"))

  def encode(str: Array[Byte]) =
    Base64.encodeBase64URLSafeString(str)

  def decode(str: String) =
    new String(Base64.decodeBase64(str))

  def decodeBytes(str: String) =
    Base64.decodeBase64(str)

  def isValid(str: String) =
    Base64.isBase64(str)
}