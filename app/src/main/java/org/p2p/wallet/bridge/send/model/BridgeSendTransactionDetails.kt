package org.p2p.wallet.bridge.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.SolAddress
import org.p2p.wallet.bridge.model.BridgeFee

@Parcelize
data class BridgeSendTransactionDetails(
    val id: String,
    val userWallet: SolAddress,
    val recipient: SolAddress,
    val amount: BridgeFee,
    val fees: BridgeSendFees,
    val status: BridgeSendTransactionStatus
) : Parcelable
