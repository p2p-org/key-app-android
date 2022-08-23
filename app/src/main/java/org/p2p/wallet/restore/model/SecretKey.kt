package org.p2p.wallet.restore.model

import android.os.Parcelable
import org.p2p.wallet.utils.emptyString
import kotlinx.parcelize.Parcelize

@Parcelize
data class SecretKey(
    val text: String = emptyString(),
    val isValid: Boolean = true
) : Parcelable
