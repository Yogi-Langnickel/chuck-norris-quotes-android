package com.yogi.chucknorris

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yogi.chucknorris.data.local.AndroidBattleScoreStore
import com.yogi.chucknorris.data.local.AndroidThemePreferenceStore
import com.yogi.chucknorris.ui.screens.MainScreen
import com.yogi.chucknorris.ui.theme.ChuckNorrisTheme
import com.yogi.chucknorris.viewmodel.QuoteViewModel
import com.yogi.chucknorris.data.repository.QuoteRepository
import com.yogi.chucknorris.data.service.ApiService
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Create the Ktor client instance
        val ktorClient = HttpClient(Android) {
            install(ContentNegotiation) {
                gson()
            }
        }

        // 2. Inject the client into the service, then into the repository
        val apiService = ApiService(client = ktorClient)
        val repository = QuoteRepository(apiService)
        val battleScoreStore = AndroidBattleScoreStore(
            getSharedPreferences("quote_battle_scores", MODE_PRIVATE)
        )
        val themePreferenceStore = AndroidThemePreferenceStore(
            getSharedPreferences("quote_battle_theme", MODE_PRIVATE)
        )

        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var darkThemeOverride by remember {
                mutableStateOf(themePreferenceStore.darkThemeOverride)
            }
            val isDarkTheme = darkThemeOverride ?: systemDarkTheme

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
