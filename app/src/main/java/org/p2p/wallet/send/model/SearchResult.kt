package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class SearchResult(open val searchAddress: SearchAddress) : Parcelable {

    @Parcelize
    data class Wrong(override val searchAddress: SearchAddress) : SearchResult(searchAddress)

    @Parcelize
    data class AddressOnly(override val searchAddress: SearchAddress) : SearchResult(searchAddress)

    @Parcelize
    data class Full(override val searchAddress: SearchAddress, val username: String) : SearchResult(searchAddress)

    @Parcelize
    data class EmptyBalance(override val searchAddress: SearchAddress) : SearchResult(searchAddress)
}
