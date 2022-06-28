package org.p2p.wallet.send.model

import kotlinx.parcelize.IgnoredOnParcel
import org.p2p.solanaj.utils.PublicKeyValidator
import org.p2p.wallet.renbtc.utils.BitcoinAddressValidator

data class Target constructor(
    val value: String
) {

    companion object {
        private const val USERNAME_MAX_LENGTH = 15
        private const val P2P_DOMAIN = ".p2p.sol"
        private const val SOL_DOMAIN = ".sol"
    }

    enum class Validation {
        EMPTY,
        INVALID,
        USERNAME,
        SOL_ADDRESS,
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
            return when {
                lowercaseValue.endsWith(P2P_DOMAIN) ||
                    lowercaseValue.endsWith(SOL_DOMAIN) ||
                    lowercaseValue.contains(".") -> {
                    val firstDotIndex = lowercaseValue.indexOf(".")
                    if (firstDotIndex > 0) {
                        lowercaseValue.substring(0, firstDotIndex)
                    } else {
                        lowercaseValue
                    }
                }
                else -> lowercaseValue.replace(".", "")
            }
        }

    @IgnoredOnParcel
    val validation: Validation
        get() {
            return when {
                trimmedUsername.length in 1..USERNAME_MAX_LENGTH -> Validation.USERNAME
                PublicKeyValidator.isValid(value) -> Validation.SOL_ADDRESS
                BitcoinAddressValidator.isValid(value) -> Validation.BTC_ADDRESS
                value.isEmpty() -> Validation.EMPTY
                else -> Validation.INVALID
            }
        }

    @IgnoredOnParcel
    val networkType: NetworkType
        get() = if (validation == Validation.BTC_ADDRESS) NetworkType.BITCOIN else NetworkType.SOLANA
}
