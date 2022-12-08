package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.user.model.TokenData

sealed class SearchResult(open val addressState: AddressState) : Parcelable {

    @Parcelize
    data class InvalidAddress(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class AddressOnly(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class UsernameFound(override val addressState: AddressState, val username: String) : SearchResult(addressState)

    @Parcelize
    data class EmptyBalance(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class InvalidResult(
        override val addressState: AddressState,
        val errorMessage: String,
        val tokenData: TokenData? = null,
        val description: String? = null
    ) : SearchResult(addressState)
}
