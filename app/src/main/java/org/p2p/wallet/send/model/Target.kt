package org.p2p.wallet.send.model

import kotlinx.parcelize.IgnoredOnParcel

data class Target constructor(
    val value: String
) {

    companion object {
        private const val ADDRESS_MIN_LENGTH = 24
        private const val USERNAME_MAX_LENGTH = 15

        private const val P2P_DOMAIN = ".p2p.sol"
        private const val SOL_DOMAIN = ".sol"
    }

    enum class Validation {
        EMPTY,
        INVALID,
        USERNAME,
        ADDRESS;
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
                lowercaseValue.endsWith(P2P_DOMAIN) || lowercaseValue.endsWith(SOL_DOMAIN) -> lowercaseValue
                else -> lowercaseValue.replace(".", "")
            }
        }

    @IgnoredOnParcel
    val validation: Validation
        get() {
            val formatted = trimmedUsername
            return when {
                formatted.length in 1..USERNAME_MAX_LENGTH -> Validation.USERNAME
                formatted.length >= ADDRESS_MIN_LENGTH -> Validation.ADDRESS
                formatted.isEmpty() -> Validation.EMPTY
                else -> Validation.INVALID
            }
        }
}
