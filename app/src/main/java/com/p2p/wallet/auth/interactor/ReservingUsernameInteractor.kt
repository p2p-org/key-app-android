package com.p2p.wallet.auth.interactor

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReservingUsernameInteractor(
    private val sharedPreferences: SharedPreferences,
) {

    suspend fun checkUsername(username: String): String = withContext(Dispatchers.IO) {
        ""
    }

    suspend fun registerUsername(username: String): String = withContext(Dispatchers.IO) {
         ""
    }

}