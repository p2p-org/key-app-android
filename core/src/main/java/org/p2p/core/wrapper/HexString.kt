package org.p2p.core.wrapper

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HexString(val rawValue: String): Parcelable
