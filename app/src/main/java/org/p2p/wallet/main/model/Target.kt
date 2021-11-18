package org.p2p.wallet.main.model

import kotlinx.parcelize.IgnoredOnParcel

data class Target(
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
    val trimmedValue: String
        get() {
            val dotIndex = value.indexOfFirst { it == '.' }
            return if (dotIndex != -1) value.substring(0, dotIndex) else value
        }

    @IgnoredOnParcel
    val validation: Validation
        get() {
            val formatted = trimmedValue
            return when {
                formatted.length in 1..USERNAME_MAX_LENGTH -> Validation.USERNAME
                formatted.length >= ADDRESS_MIN_LENGTH -> Validation.ADDRESS
                formatted.isEmpty() -> Validation.EMPTY
                else -> Validation.INVALID
            }
        }
}