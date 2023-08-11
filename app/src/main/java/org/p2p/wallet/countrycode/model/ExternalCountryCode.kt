package org.p2p.wallet.countrycode.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExternalCountryCode(
    val countryName: String,
    val nameCodeAlpha2: String,
    val nameCodeAlpha3: String,
    val flagEmoji: String?,
    val isStrigaAllowed: Boolean,
    val isMoonpayAllowed: Boolean
) : Parcelable
