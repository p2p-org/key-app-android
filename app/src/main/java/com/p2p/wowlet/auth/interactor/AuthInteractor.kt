package com.p2p.wowlet.auth.interactor

import android.content.SharedPreferences

// todo: refactor [PreferenceServiceImpl] and change key value
const val KEY_PIN_CODE = "pinCodeKey"

class AuthInteractor(
    private val sharedPreferences: SharedPreferences
) {

    fun isAuthorized() = with(sharedPreferences) {
        contains(KEY_PIN_CODE)
    }
}