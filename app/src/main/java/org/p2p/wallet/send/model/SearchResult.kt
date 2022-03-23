package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SearchResult(
    open val address: String
) : Parcelable {

    @Parcelize
    data class Wrong(override val address: String) : SearchResult(address)

    @Parcelize
    data class AddressOnly(override val address: String) : SearchResult(address)

    @Parcelize
    data class Full(override val address: String, val username: String) : SearchResult(address)

    @Parcelize
    data class EmptyBalance(override val address: String) : SearchResult(address)
}
