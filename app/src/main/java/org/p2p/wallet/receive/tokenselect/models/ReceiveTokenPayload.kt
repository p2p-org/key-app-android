package org.p2p.wallet.receive.tokenselect.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.TokenData

@Parcelize
data class ReceiveTokenPayload(
    val tokenData: TokenData,
    val isErc20Token: Boolean
) : Parcelable
