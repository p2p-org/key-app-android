package org.p2p.wallet.infrastructure.swap

import androidx.core.content.edit
import android.content.SharedPreferences
import org.p2p.core.crypto.Base58String

private const val KEY_TOKEN_A_MINT = "KEY_TOKEN_A_MINT"
private const val KEY_TOKEN_B_MINT = "KEY_TOKEN_B_MINT"
private const val KEY_SWAP_TOKENS_FETCH_DATE = "KEY_SWAP_TOKENS_FETCH_DATE"

class JupiterSwapStorage(
    private val sharedPreferences: SharedPreferences,
) : JupiterSwapStorageContract {
    override var savedTokenAMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_A_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenA) ?: remove(KEY_TOKEN_A_MINT)
        }

    override var savedTokenBMint: Base58String?
        get() = sharedPreferences.getString(KEY_TOKEN_B_MINT, null)?.let(::Base58String)
        set(value) {
            value?.let(::saveTokenB) ?: remove(KEY_TOKEN_B_MINT)
        }

    override var swapTokensFetchDateMillis: Long?
        get() = sharedPreferences.getLong(KEY_SWAP_TOKENS_FETCH_DATE, 0L).takeIf { it != 0L }
        set(value) {
            value?.let(::saveSwapTokensFetchDate) ?: remove(KEY_SWAP_TOKENS_FETCH_DATE)
        }

    override fun clear() {
        savedTokenAMint = null
        savedTokenBMint = null
        // no need to clear swapTokensFetchDateMillis at any point
    }

    private fun saveTokenA(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_A_MINT, mintAddress.base58Value) }
    }

    private fun saveTokenB(mintAddress: Base58String) {
        sharedPreferences.edit { putString(KEY_TOKEN_B_MINT, mintAddress.base58Value) }
    }

    private fun saveSwapTokensFetchDate(dateMillis: Long) {
        sharedPreferences.edit { putLong(KEY_SWAP_TOKENS_FETCH_DATE, dateMillis) }
    }

    private fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }
}
