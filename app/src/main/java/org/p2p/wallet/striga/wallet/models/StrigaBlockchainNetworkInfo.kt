package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param name is the name of the token and current network, e.g. "USD Coin Test (Goerli)"
 */
@Parcelize
data class StrigaBlockchainNetworkInfo(
    val name: String,
    val contractAddress: String?,
    val type: String?
) : Parcelable
