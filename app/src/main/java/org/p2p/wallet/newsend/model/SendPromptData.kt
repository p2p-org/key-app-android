package org.p2p.wallet.newsend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.token.Token

@Parcelize
data class SendPromptData(
    val approximateFeeUsd: String,
    val alternativeFeePayerTokens: List<Token.Active>,
    val feePayerToken: Token.Active
) : Parcelable {

    val feePayerSymbol: String = feePayerToken.tokenSymbol
}
