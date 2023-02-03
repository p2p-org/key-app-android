package org.p2p.wallet.transaction.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.newsend.model.SendFeeTotal
import java.util.Date

@Parcelize
class NewShowProgress(
    val date: Date,
    val tokenUrl: String,
    val amountTokens: String,
    val amountUsd: String,
    val recipient: String,
    val totalFee: SendFeeTotal
) : Parcelable
