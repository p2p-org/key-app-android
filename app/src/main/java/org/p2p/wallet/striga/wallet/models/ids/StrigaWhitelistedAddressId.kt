package org.p2p.wallet.striga.wallet.models.ids

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class StrigaWhitelistedAddressId(val value: String) : Parcelable
