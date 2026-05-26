package com.yogi.quotebattleroyal

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogi.quotebattleroyal.data.local.AndroidBattleScoreStore
import com.yogi.quotebattleroyal.data.local.AndroidThemePreferenceStore
import com.yogi.quotebattleroyal.ui.screens.MainScreen
import com.yogi.quotebattleroyal.ui.theme.ChuckNorrisTheme
import com.yogi.quotebattleroyal.viewmodel.QuoteViewModel
import com.yogi.quotebattleroyal.data.repository.QuoteRepository
import com.yogi.quotebattleroyal.data.service.ApiService
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.gson.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Create the Ktor client instance
        val ktorClient = HttpClient(Android) {
            install(HttpTimeout) {
                connectTimeoutMillis = 5_000
                socketTimeoutMillis = 8_000
                requestTimeoutMillis = 8_000
            }
            install(ContentNegotiation) {
                gson()
            }
        }

        // 2. Inject the client into the service, then into the repository
        val apiService = ApiService(client = ktorClient)
        val repository = QuoteRepository(
            factService = apiService,
            prefetchEnabled = true
        )
        val battleScoreStore = AndroidBattleScoreStore(
            getSharedPreferences("quote_battle_scores", MODE_PRIVATE)
        )
        val themePreferenceStore = AndroidThemePreferenceStore(
            getSharedPreferences("quote_battle_theme", MODE_PRIVATE)
        )

        setContent {
            var darkThemeOverride by remember {
                mutableStateOf(themePreferenceStore.darkThemeOverride)
            }
            val isDarkTheme = darkThemeOverride ?: true

            SideEffect {
                val systemBarStyle = if (isDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle
                )
            }

            ChuckNorrisTheme(darkTheme = isDarkTheme) {
                // 3. Use the factory to provide the repository to the ViewModel
                val quoteViewModel: QuoteViewModel = viewModel(
                    factory = QuoteViewModel.provideFactory(repository, battleScoreStore)
                )
                MainScreen(
                    quoteViewModel = quoteViewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeToggled = {
                        darkThemeOverride = !isDarkTheme
                        themePreferenceStore.darkThemeOverride = darkThemeOverride
                    }
                )
            }
        }
    }
}
