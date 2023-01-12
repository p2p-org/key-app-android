package org.p2p.wallet.sell.ui.lock

import android.os.Parcelable
import org.p2p.solanaj.utils.crypto.Base58Utils
import org.p2p.wallet.moonpay.serversideapi.response.SellTransactionStatus
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SellTransactionViewDetails(
    val transactionId: String,
    val status: SellTransactionStatus,
    val formattedSolAmount: String,
    val formattedUsdAmount: String,
    val receiverAddress: String,
) : Parcelable {
    @IgnoredOnParcel
    val isReceiverAddressWallet: Boolean
        get() = Base58Utils.isValidBase58(receiverAddress)
}
