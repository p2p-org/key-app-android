package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import java.util.Date

private const val EMPTY_BALANCE = 0L

sealed class SearchResult(open val addressState: AddressState) : Parcelable {

    @Parcelize
    data class InvalidAddress(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class AddressOnly constructor(
        override val addressState: AddressState,
        val sourceToken: Token.Active? = null,
        val date: Date? = null,
        val balance: Long = EMPTY_BALANCE
    ) : SearchResult(addressState) {
        @IgnoredOnParcel
        val isEmpty = balance == EMPTY_BALANCE
    }

    @Parcelize
    data class UsernameFound(
        override val addressState: AddressState,
        val username: String,
        val date: Date? = null
    ) : SearchResult(addressState)

    @Parcelize
    data class InvalidResult(
        override val addressState: AddressState,
        val errorMessage: String,
        val tokenData: TokenData? = null,
        val description: String? = null,
        val canReceiveAndBuy: Boolean = false
    ) : SearchResult(addressState)
}
