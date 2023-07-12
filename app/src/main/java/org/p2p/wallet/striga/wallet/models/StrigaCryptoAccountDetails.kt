package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId

@Parcelize
data class StrigaCryptoAccountDetails(
    val accountId: StrigaAccountId,
    val currency: StrigaNetworkCurrency,
    val depositAddress: String,
    val network: StrigaBlockchainNetworkInfo,
) : Parcelable
