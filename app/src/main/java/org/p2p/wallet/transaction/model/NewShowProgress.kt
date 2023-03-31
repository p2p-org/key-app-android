package org.p2p.wallet.transaction.model

import android.os.Parcelable
import java.util.Date
import kotlinx.parcelize.Parcelize
import org.p2p.core.model.TextHighlighting

@Parcelize
class NewShowProgress(
    val date: Date,
    val tokenUrl: String,
    val amountTokens: String,
    val amountUsd: String?,
    val recipient: String?,
    val totalFees: List<TextHighlighting>?
) : Parcelable
