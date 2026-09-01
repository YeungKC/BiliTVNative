package com.kirin.bilitv.core.player

import com.kirin.bilitv.core.network.BiliHeaders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BiliPlaybackHeadersTest {
  @Test
  fun apiAndMediaMapsUseSeparateUserAgents() {
    val headers = BiliPlaybackHeaders(sessData = null, biliJct = null)
    val apiUserAgent = headers.asMap()["User-Agent"]
    val mediaUserAgent = headers.asMediaMap()["User-Agent"]

    assertEquals(BiliHeaders.ApiUserAgent, apiUserAgent)
    assertEquals(BiliHeaders.UserAgent, mediaUserAgent)
    assertNotEquals(apiUserAgent, mediaUserAgent)
  }
}
