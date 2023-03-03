package org.p2p.wallet.infrastructure.swap

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.wallet.utils.Base58String

private const val KEY_TOKEN_A_MINT = "KEY_TOKEN_A_MINT"
private const val KEY_TOKEN_B_MINT = "KEY_TOKEN_B_MINT"

class JupiterSelectedSwapTokenStorage(
    private val sharedPreferences: SharedPreferences
) : JupiterSelectedSwapTokenStorageContract {
    override var savedTokenAMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_A_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenA)
        }

    override var savedTokenBMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_B_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenB)
        }

    private fun saveTokenA(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_A_MINT, mintAddress.base58Value) }
    }

    private fun saveTokenB(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_B_MINT, mintAddress.base58Value) }
    }
}
