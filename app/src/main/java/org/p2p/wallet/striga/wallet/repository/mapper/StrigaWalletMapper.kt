package org.p2p.wallet.striga.wallet.repository.mapper

import org.p2p.core.utils.STRIGA_FIAT_DECIMALS
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.striga.StrigaUserConstants.USER_FILTER_START_DATE
import org.p2p.wallet.striga.common.StrigaUserIdProvider
import org.p2p.wallet.striga.wallet.api.request.StrigaEnrichAccountRequest
import org.p2p.wallet.striga.wallet.api.request.StrigaUserWalletsRequest
import org.p2p.wallet.striga.wallet.api.response.StrigaEnrichCryptoAccountResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaEnrichFiatAccountResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletAccountResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletDetailsResponse
import org.p2p.wallet.striga.wallet.api.response.StrigaUserWalletsResponse
import org.p2p.wallet.striga.wallet.models.StrigaBlockchainNetworkInfo
import org.p2p.wallet.striga.wallet.models.StrigaCryptoAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountStatus
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.StrigaUserWalletAccount
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountBankLink
import org.p2p.wallet.striga.wallet.models.StrigaWalletAccountCurrency
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWalletId

class StrigaWalletMapper(
    private val userIdProvider: StrigaUserIdProvider
) {

    private companion object {
        private const val EUR_ACCOUNT_NAME = "EUR"
        private const val USDC_ACCOUNT_NAME = "USDC"
        private const val UNLINKED_BANK_ACCOUNT_VALUE = "UNLINKED"
    }

    fun toNetworkEnrichAccount(accountId: StrigaAccountId): StrigaEnrichAccountRequest = StrigaEnrichAccountRequest(
        userId = userIdProvider.getUserIdOrThrow(),
        accountId = accountId.value
    )

    fun toNetworkUserWallet(): StrigaUserWalletsRequest = StrigaUserWalletsRequest(
        userId = userIdProvider.getUserIdOrThrow(),
        startDate = USER_FILTER_START_DATE,
        endDate = System.currentTimeMillis(),
        page = 1 // always 1
    )

    fun fromNetwork(response: StrigaEnrichFiatAccountResponse): StrigaFiatAccountDetails = with(response) {
        StrigaFiatAccountDetails(
            currency = currency,
            status = StrigaFiatAccountStatus.from(status),
            internalAccountId = internalAccountId,
            bankName = bankName,
            bankCountry = bankCountry,
            bankAddress = bankAddress,
            bankAccountHolderName = bankAccountHolderName,
            iban = iban,
            bic = bic,
            accountNumber = accountNumber,
            provider = provider,
            paymentType = paymentType,
            isDomesticAccount = isDomesticAccount,
            routingCodeEntries = routingCodeEntries,
            payInReference = payInReference,
        )
    }

    fun fromNetwork(response: StrigaEnrichCryptoAccountResponse): StrigaCryptoAccountDetails = with(response) {
        StrigaCryptoAccountDetails(
            accountId = StrigaAccountId(accountId),
            currency = StrigaNetworkCurrency.valueOf(currency),
            depositAddress = depositAddress,
            network = StrigaBlockchainNetworkInfo(
                name = network.name,
                contractAddress = network.contractAddress,
                type = network.type,
            )
        )
    }

    fun fromNetwork(response: StrigaUserWalletsResponse): StrigaUserWallet {
        val userId = userIdProvider.getUserIdOrThrow()

        require(response.wallets.isNotEmpty()) {
            "Wallets should be not empty: they are created when user $userId is created"
        }
        // no support for multiple wallets so we get first
        val activeWallet: StrigaUserWalletDetailsResponse = response.wallets.first()

        return StrigaUserWallet(
            userId = userId,
            walletId = StrigaWalletId(activeWallet.walletId),
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
            availableBalanceLamports = accountDetails.availableBalance.amount,
            availableBalance = accountDetails.availableBalance.amount.fromLamports(STRIGA_FIAT_DECIMALS),
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
