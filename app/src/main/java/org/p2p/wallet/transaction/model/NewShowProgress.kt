package org.p2p.wallet.transaction.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
class NewShowProgress(
    val date: Date,
    val tokenUrl: String,
    val amountTokens: String,
    val amountUsd: String,
    val recipient: String,
    val totalFee: String
) : Parcelable
