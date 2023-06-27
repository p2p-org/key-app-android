package org.p2p.wallet.striga.wallet.repository

import java.math.BigInteger
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.models.StrigaFiatAccountDetails
import org.p2p.wallet.striga.wallet.models.StrigaInitiateOnchainWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaNetworkCurrency
import org.p2p.wallet.striga.wallet.models.StrigaUserWallet
import org.p2p.wallet.striga.wallet.models.StrigaWhitelistedAddressItem
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId

interface StrigaWalletRepository {

    /**
     * @param userId Striga user id
     * @param sourceAccountId Source account to withdraw from
     * @param amount The amount denominated in the smallest divisible unit of the sending currency.
     * If source account is crypto (BTC, ETH or BNB) then the amount is in wei/satoshi
     * if source account is fiat (EUR) or stable coin (USD[T/C]) then the amount is in cents
     *
     * Error codes that might be returned by striga:
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.USER_LIMIT_EXCEEDED] - amount is too big
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.BELOW_MINIMUM_AMOUNT] - amount is too small
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.INVALID_DESTINATION] - addressId is invalid
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.INSUFFICIENT_BALANCE] - not enough balance
     */
    suspend fun initiateOnchainWithdrawal(
        userId: String,
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger,
    ): StrigaDataLayerResult<StrigaInitiateOnchainWithdrawalDetails>

    /**
     * Calling this method twice for the same address will trigger an error:
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.ADDRESS_ALREADY_WHITELISTED] - address already exists
     *
     * @param userId Striga user id
     * @param address The address to whitelist. It must correspond to the [currency]
     * @param currency One of the supported currency
     * @param label An optional label to tag your address. Must be unique. A string up to 30 characters.
     */
    suspend fun addWhitelistedAddress(
        userId: String,
        address: String,
        currency: StrigaNetworkCurrency,
        label: String? = null
    ): StrigaDataLayerResult<StrigaWhitelistedAddressItem>

    /**
     * Get all whitelisted users' addresses
     *
     * @param userId Striga user id
     * @param label An optional label to filter by
     */
    suspend fun getWhitelistedAddresses(
        userId: String,
        label: String? = null
    ): StrigaDataLayerResult<List<StrigaWhitelistedAddressItem>>

    /**
     * Call enrichAccount to get deposit information
     *
     * @param userId Striga user id
     * @param accountId The account id to get details for. !! Must be a fiat account (i.e. EUR) !!
     */
    suspend fun getFiatAccountDetails(
        userId: String,
        accountId: StrigaAccountId
    ): StrigaDataLayerResult<StrigaFiatAccountDetails>

    suspend fun getUserWallet(): StrigaDataLayerResult<StrigaUserWallet?>
}
