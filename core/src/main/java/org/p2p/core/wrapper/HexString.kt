package org.p2p.core.wrapper

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class HexString(val rawValue: String): Parcelable
