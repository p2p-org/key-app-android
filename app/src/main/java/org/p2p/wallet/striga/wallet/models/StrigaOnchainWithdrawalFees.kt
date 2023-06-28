package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import java.math.BigDecimal
import java.math.BigInteger
import kotlinx.parcelize.Parcelize

@Parcelize
data class StrigaOnchainWithdrawalFees(
    val totalFee: BigInteger,
    val networkFee: BigInteger,
    val ourFee: BigInteger,
    val theirFee: BigInteger,
    val feeCurrency: StrigaNetworkCurrency,
    val gasLimit: BigInteger,
    val gasPrice: BigDecimal,
) : Parcelable
