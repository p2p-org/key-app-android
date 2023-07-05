package org.p2p.wallet.striga.wallet.models

import java.math.BigDecimal
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId

class StrigaUserWalletAccount(
    val accountId: StrigaAccountId,
    // todo (leave comment on PR if you see this): convert to enum when all statuses are found
    val accountStatus: String,
    val accountCurrency: StrigaWalletAccountCurrency,
    val parentWalletId: String,
    val ownerId: String,
    val rootFiatCurrency: String,
    val ownerType: String,
    val availableBalance: BigDecimal,
    val balanceUnit: String,
    val linkedBankAccount: StrigaWalletAccountBankLink,
) {
    fun availableBalanceWithUnits(): String = "$availableBalance $balanceUnit"
}

sealed interface StrigaWalletAccountBankLink {
    data class Linked(val value: String) : StrigaWalletAccountBankLink
    object Unlinked : StrigaWalletAccountBankLink
}

/**
 * Striga provides only 1 wallet with two accounts - USDC and EUR
 */
enum class StrigaWalletAccountCurrency(val currencyName: String) {
    USDC("USDC"), EUR("EUR"), OTHER("OTHER")
}
