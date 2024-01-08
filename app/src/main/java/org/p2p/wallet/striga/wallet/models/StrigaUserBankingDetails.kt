package org.p2p.wallet.striga.wallet.models

data class StrigaUserBankingDetails(
    val bankingBic: String?,
    val bankingIban: String?,
    val bankingFullName: String,
)
