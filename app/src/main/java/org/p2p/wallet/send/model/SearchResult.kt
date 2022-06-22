package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SearchResult(open val addressState: AddressState) : Parcelable {

    @Parcelize
    data class Wrong(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class AddressOnly(override val addressState: AddressState) : SearchResult(addressState)

    @Parcelize
    data class Full(override val addressState: AddressState, val username: String) : SearchResult(addressState)

    @Parcelize
    data class EmptyBalance(override val addressState: AddressState) : SearchResult(addressState)
}
