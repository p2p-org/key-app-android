package org.p2p.wallet.newsend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface LinkGenerationState : Parcelable {
    @Parcelize
    object Error : LinkGenerationState

    @Parcelize
    data class Success(
        val tokenSymbol: String,
        val temporaryAccountPublicKey: String,
        val formattedLink: String,
        val amount: String
    ) : LinkGenerationState
}
