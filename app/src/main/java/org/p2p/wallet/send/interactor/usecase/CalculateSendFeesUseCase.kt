package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import java.math.BigInteger
import kotlin.coroutines.cancellation.CancellationException
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.repository.SendServiceRepository
import org.p2p.wallet.utils.toPublicKey

class CalculateSendFeesUseCase(
    private val amountRepository: RpcAmountRepository,
    private val sendServiceRepository: SendServiceRepository,
    private val getFeesInPayingTokenUseCase: GetFeesInPayingTokenUseCase,
    private val addressInteractor: TransactionAddressInteractor,
) {

    suspend fun execute(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        searchResult: SearchResult,
        @Suppress("UNUSED_PARAMETER") useCache: Boolean = true
    ): FeeCalculationState {
        return try {
            val minRentExemption = getMinRentExemption(sourceToken)
            val alternativeMinRentExemption = getSendServiceMinRentExemptionForSameTokens(sourceToken, feePayerToken)

            val feesInSol = getTransactionFeesInSol(
                minRentExemption = minRentExemption,
                sourceToken = sourceToken,
                feePayerToken = feePayerToken,
                searchResult = searchResult
            )

            if (feesInSol.totalFeeLamports.isZero()) {
                Timber.i("Total fees are zero!")
                return FeeCalculationState.NoFees
            }

            val feesInFeePayerToken = getTransactionFeesInToken(
                targetToken = feePayerToken,
                feesInSol = feesInSol,
                alternativeAccountCreationFee = alternativeMinRentExemption
            )

            val feesInSourceToken = getTransactionFeesInToken(
                targetToken = sourceToken,
                feesInSol = feesInSol,
                alternativeAccountCreationFee = alternativeMinRentExemption
            )

            // it is incorrect to return fees in sol if there is some error happened
            // because we would add apples to oranges when choosing fee payer token
            require(feesInFeePayerToken != null && feesInSourceToken != null) {
                buildString {
                    append("Cannot calculate transaction fees in source (${sourceToken.tokenSymbol})")
                    append("and fee payer token ${feePayerToken.tokenSymbol}")
                }
            }

            FeeCalculationState.Success(
                fee = FeeRelayerFee(
                    feesInSol = feesInSol,
                    feesInFeePayerToken = feesInFeePayerToken,
                    feesInSourceToken = feesInSourceToken,
                    expectedFee = feesInSol
                )
            )
        } catch (e: CancellationException) {
            Timber.i("Fee calculation cancelled")
            return FeeCalculationState.Cancelled
        } catch (e: Throwable) {
            Timber.i(e, "Failed to calculateFeesForFeeRelayer")
            return FeeCalculationState.Error(e)
        }
    }

    /**
     * this is a special case, because if we have fee payer token the same as source token
     * we have to be very precise with amounts we use
     * since the math is calculated manually when "max" button is pressed
     * transaction will fail if account creation fee in token has incorrect value (rates in usd I guess)
     * given this, we just make request to send-service to see what "token_account_rent" it returns
     *
     * todo: it looks like we don't have correct prices for tokens at a moment
     *
     * @return zero if tokens are different, or min rent exemption in source token
     */
    private suspend fun getSendServiceMinRentExemptionForSameTokens(
        sourceToken: Token.Active,
        feePayerToken: Token.Active
    ): BigInteger {
        if (sourceToken.isSOL || sourceToken.mintAddress != feePayerToken.mintAddress) {
            return BigInteger.ZERO
        }

        return sendServiceRepository
            .getTokenRentExemption(
                userWallet = sourceToken.publicKey.toBase58Instance(),
                token = sourceToken,
                // return in TOKEN
                returnInToken = true
            )
    }

    /**
     * token2022 may has higher account creation fee as it takes more data for account
     */
    private suspend fun getMinRentExemption(sourceToken: Token.Active) = if (sourceToken.isToken2022) {
        sendServiceRepository
            .getTokenRentExemption(
                userWallet = sourceToken.publicKey.toBase58Instance(),
                token = sourceToken,
                // return in SOL
                returnInToken = false
            )
            .takeIf(BigInteger::isNotZero)
            ?: amountRepository.getMinBalanceForRentExemption(
                TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
            )
    } else {
        amountRepository.getMinBalanceForRentExemption(
            TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
        )
    }

    private suspend fun getTransactionFeesInSol(
        minRentExemption: BigInteger,
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        searchResult: SearchResult,
    ): FeeAmount {
        val lamportsPerSignature: BigInteger = amountRepository.getLamportsPerSignature(null)
        var transactionFeeInSol = BigInteger.ZERO

        // owner's signature
        transactionFeeInSol += lamportsPerSignature

        // feePayer's signature
        if (!feePayerToken.isSOL) {
            Timber.i("Fee payer is not sol, adding $lamportsPerSignature for fee")
            transactionFeeInSol += lamportsPerSignature
        }

        val shouldCreateAccount = checkAccountCreationIsRequired(sourceToken, searchResult.address)
        Timber.i("Should create account = $shouldCreateAccount")

        val accountCreationFeeInSol = if (shouldCreateAccount) minRentExemption else BigInteger.ZERO

        return FeeAmount(
            transactionFee = transactionFeeInSol,
            accountCreationFee = accountCreationFeeInSol,
        ).copy(
            // todo: while we use send service, we don't pay network fee
            transactionFee = BigInteger.ZERO
        )
    }

    private suspend fun getTransactionFeesInToken(
        targetToken: Token.Active,
        feesInSol: FeeAmount,
        alternativeAccountCreationFee: BigInteger
    ): FeeAmount? {
        return getFeesInPayingTokenUseCase.execute(
            targetToken = targetToken,
            transactionFeeInSol = feesInSol.transactionFee,
            accountCreationFeeInSol = feesInSol.accountCreationFee
        )?.let {
            // if local calculation in token represents lower fee, than use send-service fee
            it.copy(accountCreationFee = maxOf(it.accountCreationFee, alternativeAccountCreationFee))
        }
    }

    private suspend fun checkAccountCreationIsRequired(
        token: Token.Active,
        recipient: String
    ): Boolean {
        return token.mintAddress != Constants.WRAPPED_SOL_MINT && addressInteractor.findSplTokenAddressData(
            mintAddress = token.mintAddress,
            destinationAddress = recipient.toPublicKey(),
            programId = token.programId?.toPublicKey() ?: TokenProgram.PROGRAM_ID
        ).shouldCreateAccount
    }
}
