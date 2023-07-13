package org.p2p.wallet.receive.tokenselect.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.TokenMetadata

@Parcelize
data class ReceiveTokenPayload(
    val tokenMetadata: TokenMetadata,
    val isErc20Token: Boolean
) : Parcelable
