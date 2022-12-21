package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import java.util.Date

sealed class SearchResult(open val addressState: AddressState) : Parcelable {

    @Parcelize
    data class InvalidAddress(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class AddressOnly constructor(
        override val addressState: AddressState,
        val sourceToken: Token.Active? = null,
        val date: Date? = null
    ) : SearchResult(addressState)

    @Parcelize
    data class UsernameFound(
        override val addressState: AddressState,
        val username: String,
        val date: Date? = null
    ) : SearchResult(addressState)

    @Parcelize
    data class EmptyBalance(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class InvalidResult(
        override val addressState: AddressState,
        val errorMessage: String,
        val tokenData: TokenData? = null,
        val description: String? = null,
        val canReceiveAndBuy: Boolean = false
    ) : SearchResult(addressState)
}
