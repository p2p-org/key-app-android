package org.p2p.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Entity for cases where we need to use Pair<String,String>
 * but to put it in Parcelable an use common ways for presentation layer
 **/
@Parcelize
class TitleValue(
    val title: String,
    val value: String,
) : Parcelable
