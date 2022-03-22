package org.p2p.wallet.transaction.model

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
class ShowProgress(
    @StringRes val title: Int,
    val subTitle: String,
    val transactionId: String
) : Parcelable
