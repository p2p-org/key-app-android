package org.p2p.wallet.newsend.model

import org.p2p.solanaj.utils.PublicKeyValidator
import org.p2p.wallet.renbtc.utils.BitcoinAddressValidator
import kotlinx.parcelize.IgnoredOnParcel

data class SearchTarget(
    val value: String,
    val keyAppDomainIfUsername: String
) {

    companion object {
        private const val USERNAME_MAX_LENGTH = 15
        private const val SOL_DOMAIN = ".sol"
    }

    enum class Validation {
        EMPTY,
        INVALID,
        USERNAME,
        SOLANA_TYPE_ADDRESS,
        BTC_ADDRESS
    }

    /**
     * Removing domain from username if exists.
     * @sample test.p2p.sol -> test.p2p.sol
     * @sample test.sol -> test.sol
     * @sample test.something -> testsomething
     * */
    @IgnoredOnParcel
    val trimmedUsername: String
        get() {
            val lowercaseValue = value.lowercase().trim()
            return checkAndTrimName(lowercaseValue)
        }

    private fun checkAndTrimName(value: String): String {
        return when {
            value.endsWith(keyAppDomainIfUsername) ||
                value.endsWith(SOL_DOMAIN) ||
                value.contains(".") -> {
                val firstDotIndex = value.indexOf(".")
                if (firstDotIndex > 0) {
                    value.substring(0, firstDotIndex)
                } else {
                    value
                }
            }
            else -> {
                value.replace(".", "")
            }
        }
    }

    @IgnoredOnParcel
    val validation: Validation
        get() {
            return when {
                trimmedUsername.length in 1..USERNAME_MAX_LENGTH -> Validation.USERNAME
                PublicKeyValidator.isValid(value) -> Validation.SOLANA_TYPE_ADDRESS
                BitcoinAddressValidator.isValid(value) -> Validation.BTC_ADDRESS
                value.isEmpty() -> Validation.EMPTY
                else -> Validation.INVALID
            }
        }

    @IgnoredOnParcel
    val networkType: NetworkType
        get() = if (validation == Validation.BTC_ADDRESS) NetworkType.BITCOIN else NetworkType.SOLANA
}
