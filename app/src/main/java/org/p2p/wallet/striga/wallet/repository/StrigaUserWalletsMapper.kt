package org.p2p.wallet.striga.wallet.repository

import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletAccountResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletDetailsResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletsResponse
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountBankLink
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId

class StrigaUserWalletsMapper {
    private companion object {
        private const val EUR_ACCOUNT_NAME = "EUR"
        private const val USDC_ACCOUNT_NAME = "USDC"
        private const val UNLINKED_BANK_ACCOUNT_VALUE = "UNLINKED"
    }

    fun fromNetwork(userId: String, response: StrigaUserWalletsResponse): StrigaUserWallet {
        require(response.wallets.isNotEmpty()) {
            "Wallets should be not empty: they are created when user $userId is created"
        }
        val activeWallet: StrigaUserWalletDetailsResponse = response.wallets.first()

        return StrigaUserWallet(
            userId = userId,
            walletId = StrigaWalletId(activeWallet.walletId),
            // no support for multiple wallets so we get first
            accounts = activeWallet.accountCurrencyToDetails.map(::toDomain)
        )
    }

    private fun toDomain(entry: Map.Entry<String, StrigaUserWalletAccountResponse>): StrigaUserWalletAccount {
        val (accountCurrency, accountDetails) = entry
        return StrigaUserWalletAccount(
            accountId = StrigaAccountId(accountDetails.accountId),
            accountStatus = accountDetails.status,
            accountCurrency = mapAccountCurrency(accountCurrency),
            parentWalletId = accountDetails.parentWalletId,
            ownerId = accountDetails.ownerId,
            rootFiatCurrency = accountDetails.rootFiatCurrency,
            ownerType = accountDetails.ownerType,
            availableBalance = accountDetails.availableBalance.amount,
            balanceUnit = accountDetails.availableBalance.currencyUnits,
            linkedBankAccount = mapLinkedBankAccount(accountDetails.linkedBankAccountId)
        )
    }

    private fun mapAccountCurrency(currency: String): StrigaWalletAccountCurrency {
        return when (currency.uppercase()) {
            EUR_ACCOUNT_NAME -> StrigaWalletAccountCurrency.EUR
            USDC_ACCOUNT_NAME -> StrigaWalletAccountCurrency.USDC
            else -> StrigaWalletAccountCurrency.OTHER
        }
    }

    private fun mapLinkedBankAccount(linkedBankAccountId: String?): StrigaWalletAccountBankLink {
        return if (linkedBankAccountId == null || linkedBankAccountId == UNLINKED_BANK_ACCOUNT_VALUE) {
            StrigaWalletAccountBankLink.Unlinked
        } else {
            StrigaWalletAccountBankLink.Linked(linkedBankAccountId)
        }
    }
}
