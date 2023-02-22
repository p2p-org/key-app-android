package org.p2p.wallet.history.model.rpc

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

@Parcelize
class RpcFee(
    val totalInTokens: BigDecimal,
    val totalInUsd: BigDecimal?,
    val tokensDecimals: Int?,
    val tokenSymbol: String?
) : Parcelable
