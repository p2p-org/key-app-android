package org.p2p.wallet.restore.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SecretKey(
    val text: String = ""
) : Parcelable