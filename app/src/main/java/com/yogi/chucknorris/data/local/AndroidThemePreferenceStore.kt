package com.yogi.chucknorris.data.local

import android.content.SharedPreferences

class AndroidThemePreferenceStore(
    private val sharedPreferences: SharedPreferences
) {
    var darkThemeOverride: Boolean?
        get() {
            return if (sharedPreferences.contains(KEY_DARK_THEME)) {
                sharedPreferences.getBoolean(KEY_DARK_THEME, false)
            } else {
                null
            }
        }
        set(value) {
            sharedPreferences.edit().apply {
                if (value == null) {
                    remove(KEY_DARK_THEME)
                } else {
                    putBoolean(KEY_DARK_THEME, value)
                }
            }.apply()
        }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
    }
}
