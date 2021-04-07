package com.p2p.wowlet.auth

import android.content.SharedPreferences
import com.p2p.wowlet.domain.interactors.SplashScreenInteractor

// todo: refactor [PreferenceServiceImpl] and change key value
const val KEY_PIN_CODE = "pinCodeKey"

class AuthInteractor(
    private val sharedPreferences: SharedPreferences
) : SplashScreenInteractor {

    override fun isAuthorized() = with(sharedPreferences) {
        contains(KEY_PIN_CODE)
    }
}