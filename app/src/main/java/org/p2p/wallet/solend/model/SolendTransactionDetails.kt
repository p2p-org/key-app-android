package org.p2p.wallet.solend.model

import android.os.Parcelable
import org.p2p.wallet.utils.emptyString
import kotlinx.parcelize.Parcelize

@Parcelize
data class SolendTransactionDetails(
    val amount: String,
    val transferFee: String?, // null == free
    val fee: String,
    val total: String
) : Parcelable {

    companion object {
        val EMPTY = SolendTransactionDetails(
            amount = emptyString(),
            transferFee = null,
            fee = emptyString(),
            total = emptyString()
        )
    }
}
