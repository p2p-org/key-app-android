package org.p2p.wallet.striga.wallet.repository

import java.math.BigInteger
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.wallet.models.StrigaInitWithdrawalDetails
import org.p2p.wallet.striga.wallet.models.StrigaOnchainWithdrawalFees
import org.p2p.wallet.striga.wallet.models.ids.StrigaAccountId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWhitelistedAddressId
import org.p2p.wallet.striga.wallet.models.ids.StrigaWithdrawalChallengeId

interface StrigaWithdrawalsRepository {
    /**
     * @param sourceAccountId Source account to withdraw from
     * @param amountInUnits The amount denominated in the smallest divisible unit of the sending currency.
     * If source account is crypto (BTC, ETH or BNB) then the amount is in wei/satoshi
     * if source account is fiat (EUR) or stable coin (USD[T/C]) then the amount is in cents
     *
     * Error codes that might be returned by striga:
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.TOO_LARGE_AMOUNT] - amount is too big
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.TOO_SMALL_AMOUNT] - amount is too small
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.INVALID_DESTINATION_ADDRESS] - addressId is invalid
     * [org.p2p.wallet.striga.model.StrigaApiErrorCode.INSUFFICIENT_BALANCE] - not enough balance
     */
    suspend fun initiateOnchainWithdrawal(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amountInUnits: BigInteger
    ): StrigaDataLayerResult<StrigaInitWithdrawalDetails>

    suspend fun getOnchainWithdrawalFees(
        sourceAccountId: StrigaAccountId,
        whitelistedAddressId: StrigaWhitelistedAddressId,
        amount: BigInteger
    ): StrigaDataLayerResult<StrigaOnchainWithdrawalFees>

    suspend fun verifySms(smsCode: String, challengeId: StrigaWithdrawalChallengeId): StrigaDataLayerResult<Unit>
    suspend fun resendSms(challengeId: StrigaWithdrawalChallengeId): StrigaDataLayerResult<Unit>
}
