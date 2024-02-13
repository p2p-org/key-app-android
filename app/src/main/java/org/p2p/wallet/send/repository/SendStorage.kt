package org.p2p.wallet.send.repository

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.crypto.Base58String
import org.p2p.core.crypto.toBase58Instance

class SendStorage(
    private val prefs: SharedPreferences
) : SendStorageContract {
    private companion object {
        const val KEY_TOKEN_MINT = "TOKEN_MINT"
    }

    override fun restoreFeePayerToken(): Base58String? {
        return prefs.getString(KEY_TOKEN_MINT, null)?.toBase58Instance()
    }

    override fun saveFeePayerToken(tokenMint: Base58String) {
        prefs.edit {
            putString(KEY_TOKEN_MINT, tokenMint.base58Value)
        }
    }

    override fun removeFeePayerToken() {
        prefs.edit {
            remove(KEY_TOKEN_MINT)
        }
    }
}
