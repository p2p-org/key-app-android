package org.p2p.wallet.transaction.ui

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapTransactionBottomSheetData(
    val date: Date,
    val formattedAmountUsd: String?,
    val tokenA: SwapTransactionBottomSheetToken,
    val tokenB: SwapTransactionBottomSheetToken
) : Parcelable

@Parcelize
data class SwapTransactionBottomSheetToken(
    val tokenUrl: String,
    val tokenName: String,
    val formattedTokenAmount: String
) : Parcelable
