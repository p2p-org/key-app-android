package org.p2p.wallet.swap.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.home.model.Token

@Parcelize
class SwapConfirmData(
    val sourceToken: Token.Active,
    val destinationToken: Token,
    val sourceAmount: String,
    val sourceAmountUsd: String,
    val destinationAmount: String,
    val destinationAmountUsd: String
) : Parcelable {

    fun getFormattedSourceAmount(): String = "$sourceAmount ${sourceToken.tokenSymbol}"

    fun getFormattedSourceAmountUsd(): String = "~$$sourceAmountUsd"

    fun getFormattedDestinationAmount(): String = "$destinationAmount ${destinationToken.tokenSymbol}"

    fun getFormattedDestinationAmountUsd(): String = "~$$destinationAmountUsd"
}
