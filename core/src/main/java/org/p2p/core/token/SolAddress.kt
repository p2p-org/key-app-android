package org.p2p.core.token

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class SolAddress(val raw: String): Parcelable
