package org.p2p.core.token

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TokenMetadata(
    val mintAddress: String,
    val name: String,
    val symbol: String,
    val iconUrl: String?,
    val decimals: Int,
    val isWrapped: Boolean,
    val extensions: TokenExtension?
) : Parcelable
