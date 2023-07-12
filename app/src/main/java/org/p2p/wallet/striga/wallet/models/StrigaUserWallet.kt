package org.p2p.wallet.striga.wallet.models

import org.p2p.core.utils.isNotZero
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId

/**
 * A wallet on the Striga platform contains accounts.
 * Accounts are the lowest divisible unit of value storage and are each represented in one currency only.
 * When creating a wallet, accounts for each of your configured currencies are created and linked under that wallet.
 */
data class StrigaUserWallet(
    val walletId: StrigaWalletId,
    val userId: String,
    val accounts: List<StrigaUserWalletAccount>
) {

    val hasAvailableBalance: Boolean
        get() = accounts.any { it.availableBalance.isNotZero() }

    val eurAccount: StrigaUserWalletAccount?
        get() = accounts.firstOrNull { it.accountCurrency == StrigaWalletAccountCurrency.EUR }

    val usdcAccount: StrigaUserWalletAccount?
        get() = accounts.firstOrNull { it.accountCurrency == StrigaWalletAccountCurrency.USDC }
}
