package org.p2p.wallet.striga.wallet.models.ids

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class StrigaAccountTransactionId(val value: String) : Parcelable
