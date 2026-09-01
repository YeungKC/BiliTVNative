package com.kirin.bilitv.ui.player

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Test
class DashBaseUrlFailoverTest {
  @Test
  fun http503OnPrimaryRequestsNextDvbPriorityBaseUrl() {
    val primaryRequested = CountDownLatch(1)
    val backupRequested = CountDownLatch(1)
    val requests = Collections.synchronizedList(mutableListOf<String>())
    val dataSourceFactory = object : DataSource.Factory {
      override fun createDataSource(): DataSource {
        return RecordingDataSource(
          primaryRequested = primaryRequested,
          backupRequested = backupRequested,
          requests = requests,
        )
      }
    }
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    lateinit var player: ExoPlayer
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      player = ExoPlayer.Builder(context).build()
      player.setMediaSource(
        DashMediaSource.Factory(dataSourceFactory)
          .createMediaSource(MediaItem.fromUri(MANIFEST_URI)),
      )
      player.prepare()
      player.play()
    }

    try {
      assertTrue("primary URL was not requested", primaryRequested.await(10, TimeUnit.SECONDS))
      assertTrue("backup URL was not requested after primary 503", backupRequested.await(10, TimeUnit.SECONDS))
      assertTrue(requests.indexOf(PRIMARY_URL) < requests.indexOf(BACKUP_URL))
    } finally {
      InstrumentationRegistry.getInstrumentation().runOnMainSync {
        player.release()
      }
    }
  }

  private class RecordingDataSource(
    private val primaryRequested: CountDownLatch,
    private val backupRequested: CountDownLatch,
    private val requests: MutableList<String>,
  ) : DataSource {
    private var openedUri: Uri? = null
    private var data = ByteArray(0)
    private var readPosition = 0

    override fun addTransferListener(transferListener: TransferListener) = Unit

    override fun open(dataSpec: DataSpec): Long {
      val url = dataSpec.uri.toString()
      requests += url
      openedUri = dataSpec.uri
      when (url) {
        PRIMARY_URL -> {
          primaryRequested.countDown()
          throw unavailable(dataSpec)
        }
        BACKUP_URL -> {
          backupRequested.countDown()
          throw unavailable(dataSpec)
        }
        MANIFEST_URI -> {
          data = MANIFEST_XML.toByteArray()
          readPosition = 0
          return data.size.toLong()
        }
      }
      throw IOException("Unexpected test URL: $url")
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
      if (readPosition == data.size) {
        return C.RESULT_END_OF_INPUT
      }
      val bytesToRead = minOf(length, data.size - readPosition)
      data.copyInto(buffer, offset, readPosition, readPosition + bytesToRead)
      readPosition += bytesToRead
      return bytesToRead
    }

    override fun getUri(): Uri? = openedUri

    override fun close() {
      openedUri = null
      data = ByteArray(0)
      readPosition = 0
    }


    private fun unavailable(dataSpec: DataSpec): InvalidResponseCodeException {
      return InvalidResponseCodeException(
        503,
        "Service Unavailable",
        null,
        emptyMap(),
        dataSpec,
        byteArrayOf(),
      )
    }
  }

  private companion object {
    const val MANIFEST_URI = "https://manifest.test/manifest.mpd"
    const val PRIMARY_URL = "https://primary.test/video.mp4"
    const val BACKUP_URL = "https://backup.test/video.mp4"
    val MANIFEST_XML = """
      <?xml version="1.0" encoding="UTF-8"?>
      <MPD xmlns="urn:mpeg:dash:schema:mpd:2011"
        xmlns:dvb="urn:dvb:dash:dash-extensions:2014-1"
        type="static"
        mediaPresentationDuration="PT1S"
        minBufferTime="PT0.1S"
        profiles="urn:mpeg:dash:profile:isoff-on-demand:2011,urn:dvb:dash:profile:dvb-dash:2014">
        <Period duration="PT1S">
          <AdaptationSet id="0" contentType="video" mimeType="video/mp4" segmentAlignment="true">
            <Representation id="video" bandwidth="1000" codecs="avc1.4D401E" width="16" height="16">
              <BaseURL dvb:priority="1" dvb:weight="1" serviceLocation="primary">$PRIMARY_URL</BaseURL>
              <BaseURL dvb:priority="2" dvb:weight="1" serviceLocation="backup">$BACKUP_URL</BaseURL>
              <SegmentBase indexRange="0-0">
                <Initialization range="0-0" />
              </SegmentBase>
            </Representation>
          </AdaptationSet>
        </Period>
      </MPD>
    """.trimIndent()
  }
}
