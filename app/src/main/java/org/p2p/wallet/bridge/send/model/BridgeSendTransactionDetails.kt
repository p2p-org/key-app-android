package org.p2p.wallet.bridge.send.model

import android.os.Parcelable
import java.math.BigInteger
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.SolAddress

@Parcelize
data class BridgeSendTransactionDetails(
    val id: String,
    val userWallet: SolAddress,
    val recipient: SolAddress,
    val amount: BigInteger,
    val fees: BridgeSendFees,
    val status: BridgeSendTransactionStatus
) : Parcelable
