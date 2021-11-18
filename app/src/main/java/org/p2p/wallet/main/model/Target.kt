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

    @IgnoredOnParcel
    val validation: Validation = when {
        value.length in 1..USERNAME_MAX_LENGTH -> Validation.USERNAME
        value.length >= ADDRESS_MIN_LENGTH -> Validation.ADDRESS
        value.isEmpty() -> Validation.EMPTY
        else -> Validation.INVALID
    }
}