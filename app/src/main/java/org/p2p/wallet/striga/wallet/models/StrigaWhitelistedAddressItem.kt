package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId

@Parcelize
data class StrigaWhitelistedAddressItem(
    val id: StrigaWhitelistedAddressId,
    val status: Status,
    val address: String,
    val currency: StrigaNetworkCurrency,
    val network: StrigaBlockchainNetworkInfo
) : Parcelable {
    enum class Status {
        PENDING_ACTIVATION,
        ACTIVATED,
    }
}
