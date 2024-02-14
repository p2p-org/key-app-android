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

    override fun restore(): Base58String? {
        return prefs.getString(KEY_TOKEN_MINT, null)?.toBase58Instance()
    }

    override fun save(tokenMint: Base58String) {
        prefs.edit {
            putString(KEY_TOKEN_MINT, tokenMint.base58Value)
        }
    }

    override fun remove() {
        prefs.edit {
            remove(KEY_TOKEN_MINT)
        }
    }
}
