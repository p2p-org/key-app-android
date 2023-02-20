package org.p2p.wallet.history.model.rpc

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
class RpcHistoryAmount(
    val total: BigDecimal,
    val totalInUsd: BigDecimal?
) : Parcelable
