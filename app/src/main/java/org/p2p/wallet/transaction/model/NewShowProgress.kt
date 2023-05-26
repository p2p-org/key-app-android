package org.p2p.wallet.transaction.model

import androidx.annotation.ColorRes
import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.Parcelize
import org.p2p.core.model.TextHighlighting

@Parcelize
class NewShowProgress(
    val date: ZonedDateTime,
    val tokenUrl: String,
    val amountTokens: String,
    val amountUsd: String?,
    val recipient: String?,
    val totalFees: List<TextHighlighting>?,
    @ColorRes val amountColor: Int? = null
) : Parcelable
