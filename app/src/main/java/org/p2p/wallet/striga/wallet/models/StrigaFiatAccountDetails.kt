package org.p2p.wallet.striga.wallet.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StrigaFiatAccountDetails(
    val currency: String,
    val status: StrigaFiatAccountStatus,
    val internalAccountId: String,
    val bankCountry: String,
    val bankAddress: String,
    val iban: String,
    val bic: String,
    val accountNumber: String,
    val bankName: String,
    val bankAccountHolderName: String,
    val provider: String,
    val paymentType: String?,
    val isDomesticAccount: Boolean,

    // todo: idk what should be in these 2 fields
    val routingCodeEntries: List<String>,
    val payInReference: String?,
) : Parcelable
