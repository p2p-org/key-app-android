package org.p2p.wallet.main.model

import kotlinx.parcelize.IgnoredOnParcel

data class Target constructor(
    val value: String
) {

    companion object {
        private const val ADDRESS_MIN_LENGTH = 24
        private const val USERNAME_MAX_LENGTH = 15
    }

    enum class Validation {
        EMPTY,
        INVALID,
        USERNAME,
        ADDRESS;
    }

    /**
     * Removing domain from username if exists.
     * @sample test.p2p.sol -> test
     * @sample test.sol -> test
     * */
    @IgnoredOnParcel
    val trimmedUsername: String
        get() {
            val lowercaseValue = value.lowercase().trim()
            val dotIndex = lowercaseValue.indexOfFirst { it == '.' }
            return if (dotIndex != -1) lowercaseValue.substring(0, dotIndex) else lowercaseValue
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