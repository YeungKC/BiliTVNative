package com.kirin.bilitv.core.player

import com.kirin.bilitv.core.network.BiliHeaders

data class BiliPlaybackHeaders(
  val sessData: String?,
  val biliJct: String?,
) {
  val cookie: String?
    get() = BiliHeaders.cookie(sessData, biliJct)

  fun asMap(includeCookie: Boolean = true): Map<String, String> {
    return buildHeaders(
      userAgent = BiliHeaders.ApiUserAgent,
      includeCookie = includeCookie,
    )
  }

  fun asMediaMap(includeCookie: Boolean = true): Map<String, String> {
    return buildHeaders(
      userAgent = BiliHeaders.UserAgent,
      includeCookie = includeCookie,
    )
  }

  private fun buildHeaders(userAgent: String, includeCookie: Boolean): Map<String, String> {
    return buildMap {
      put("User-Agent", userAgent)
      put("Referer", BiliHeaders.Referer)
      put("Origin", BiliHeaders.Origin)
      if (includeCookie) {
        cookie?.let { value -> put("Cookie", value) }
      }
    }
  }
}
