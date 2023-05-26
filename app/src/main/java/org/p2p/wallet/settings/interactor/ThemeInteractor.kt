package org.p2p.wallet.settings.interactor

import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences

private const val THEME_KEY = "THEME_KEY"

class ThemeInteractor(
    private val sharedPreferences: SharedPreferences
) {
    private val defaultTheme = AppCompatDelegate.MODE_NIGHT_NO

    fun applyCurrentNightMode() {
        AppCompatDelegate.setDefaultNightMode(getNightMode())
    }

    @AppCompatDelegate.NightMode
    fun getNightMode(): Int = sharedPreferences.getInt(THEME_KEY, defaultTheme)
}

sealed class Theme(val name: String, @AppCompatDelegate.NightMode val nightMode: Int) {
    object Light : Theme("light", AppCompatDelegate.MODE_NIGHT_NO)
    object Dark : Theme("dark", AppCompatDelegate.MODE_NIGHT_YES)
    object System : Theme("system", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}
