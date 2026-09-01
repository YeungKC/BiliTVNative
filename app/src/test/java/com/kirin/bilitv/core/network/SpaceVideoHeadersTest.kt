package com.kirin.bilitv.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SpaceVideoHeadersTest {
  @Test
  fun spaceHeadersKeepWebProfileSeparateFromApiProfile() {
    val headers = spaceHeaders(
      mid = "8143590",
      sessData = "test-sessdata",
      biliJct = "test-bili-jct",
      dedeUserId = 8143590L,
      buvid3 = "test-buvid3",
      buvid4 = "test-buvid4",
    )

    assertEquals(BiliHeaders.SpaceUserAgent, headers["User-Agent"])
    assertNotEquals(BiliHeaders.ApiUserAgent, headers["User-Agent"])
    assertEquals(
      "\"Google Chrome\";v=\"147\", \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"147\"",
      headers["sec-ch-ua"],
    )
    assertEquals("https://space.bilibili.com/8143590", headers["Referer"])
  }
}
