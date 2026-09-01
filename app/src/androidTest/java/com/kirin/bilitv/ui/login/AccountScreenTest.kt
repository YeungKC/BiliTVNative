package com.kirin.bilitv.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kirin.bilitv.R
import com.kirin.bilitv.core.auth.AuthRepository
import com.kirin.bilitv.core.auth.TvLoginSigner
import com.kirin.bilitv.core.network.BiliApiClient
import com.kirin.bilitv.core.storage.SessionStore
import com.kirin.bilitv.core.storage.UserSession
import com.kirin.bilitv.ui.theme.BiliTvTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test

class AccountScreenTest {
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun logoutClearsSession() {
    val context = InstrumentationRegistry.getInstrumentation()
      .context
      .createDeviceProtectedStorageContext()
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val sessionStore = SessionStore(context)
    runBlocking {
      sessionStore.clearSession()
      sessionStore.saveSession(sessData = "test-sessdata", biliJct = "test-bili-jct")
      sessionStore.saveDeviceCookies(buvid3 = "test-buvid3", buvid4 = "test-buvid4")
      sessionStore.saveUserProfile(
        mid = 1L,
        face = "https://example.com/avatar.jpg",
        uname = "Test user",
        isVip = false,
      )
    }
    val authRepository = AuthRepository(
      apiClient = BiliApiClient(OkHttpClient(), Json),
      tvLoginSigner = TvLoginSigner(),
      sessionStore = sessionStore,
    )

    try {
      composeRule.setContent {
        BiliTvTheme {
          AccountScreen(
            userSession = UserSession(
              sessData = "test-sessdata",
              biliJct = "test-bili-jct",
              buvid3 = "test-buvid3",
              buvid4 = "test-buvid4",
              mid = 1L,
              face = "https://example.com/avatar.jpg",
              uname = "Test user",
            ),
            authRepository = authRepository,
          )
        }
      }
      composeRule.waitUntil(5_000) {
        runCatching {
          composeRule.onNode(hasClickAction(), useUnmergedTree = true).assertExists()
          true
        }.getOrDefault(false)
      }
      composeRule.onNode(
        hasText(appContext.getString(R.string.account_logout)),
        useUnmergedTree = true,
      ).assertIsDisplayed()
      composeRule.onNode(hasClickAction(), useUnmergedTree = true).performClick()
      composeRule.waitUntil(5_000) {
        runCatching {
          !runBlocking { sessionStore.session.first().isLoggedIn }
        }.getOrDefault(false)
      }
    } finally {
      runBlocking {
        sessionStore.clearSession()
      }
    }
  }
}
