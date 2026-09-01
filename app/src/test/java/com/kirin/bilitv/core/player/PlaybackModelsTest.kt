package com.kirin.bilitv.core.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackModelsTest {
  @Test
  fun manifestBaseUrlsKeepsPrimaryDeduplicatesAndCapsBackups() {
    val track = PlaybackTrack(
      id = 1,
      baseUrl = "https://primary.example/video.m4s",
      backupUrls = listOf(
        "",
        "https://backup-1.example/video.m4s",
        "https://primary.example/video.m4s",
        "https://backup-2.example/video.m4s",
        "https://backup-3.example/video.m4s",
        "https://backup-4.example/video.m4s",
        "https://backup-5.example/video.m4s",
        "https://backup-6.example/video.m4s",
        "https://backup-7.example/video.m4s",
        "https://backup-8.example/video.m4s",
      ),
      bandwidth = 1_000,
      codecs = "avc1.640028",
      width = 1_920,
      height = 1_080,
      mimeType = "video/mp4",
      segmentBase = PlaybackSegmentBase(
        initializationRange = "0-100",
        indexRange = "101-200",
      ),
    )

    assertEquals(
      listOf(
        "https://primary.example/video.m4s",
        "https://backup-1.example/video.m4s",
        "https://backup-2.example/video.m4s",
        "https://backup-3.example/video.m4s",
        "https://backup-4.example/video.m4s",
        "https://backup-5.example/video.m4s",
        "https://backup-6.example/video.m4s",
        "https://backup-7.example/video.m4s",
      ),
      track.manifestBaseUrls(),
    )
  }
}
