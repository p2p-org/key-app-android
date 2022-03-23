package org.p2p.wallet.restore.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.emptyString

@Parcelize
data class SecretKey(
    val text: String = emptyString()
) : Parcelable
