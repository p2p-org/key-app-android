package org.p2p.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TitleValue(
    val title: String,
    val value: String,
) : Parcelable
