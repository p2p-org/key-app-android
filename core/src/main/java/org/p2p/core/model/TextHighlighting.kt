package org.p2p.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TextHighlighting(
    val commonText: String,
    val highlightedText: String,
) : Parcelable
