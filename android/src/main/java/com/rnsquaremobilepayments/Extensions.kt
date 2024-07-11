package com.rnsquaremobilepayments

import com.facebook.react.bridge.ReadableMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun ReadableMap.getBooleanOr(key: String, default: Boolean): Boolean {
  return if (this.hasKey(key)) this.getBoolean(key) else default
}

fun Date.toISO8601(): String {
  val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
  iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
  val isoString = iso8601Format.format(this)
  return isoString
}
