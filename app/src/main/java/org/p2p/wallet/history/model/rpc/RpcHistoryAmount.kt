package org.p2p.wallet.history.model.rpc

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

@Parcelize
class RpcHistoryAmount(
    val total: BigDecimal,
    val totalInUsd: BigDecimal?
) : Parcelable
