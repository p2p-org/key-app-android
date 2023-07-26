package org.p2p.wallet.transaction.model

import androidx.annotation.ColorRes
import android.os.Parcelable
import org.threeten.bp.ZonedDateTime
import kotlinx.parcelize.Parcelize
import org.p2p.core.model.TextHighlighting
import org.p2p.core.model.TitleValue

@Parcelize
class NewShowProgress(
    val date: ZonedDateTime,
    val tokenUrl: String,
    val amountTokens: String,
    val amountUsd: String?,
    val data: List<TitleValue>? = null,
    val totalFees: List<TextHighlighting>? = null,
    @ColorRes val amountColor: Int? = null
) : Parcelable
