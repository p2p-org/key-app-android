package org.p2p.wallet.feerelayer.interactor

import java.math.BigInteger
import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.isZero
import org.p2p.core.utils.orZero
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.wallet.feerelayer.model.FeePoolsState
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.utils.retryRequest

class FeeRelayerInteractor(
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: NetworkEnvironmentManager,
    private val dispatchers: CoroutineDispatchers
) {

    companion object {
        private val MIN_TOP_UP_AMOUNT = BigInteger.valueOf(10000L)
    }

    /**
     *  Top up and make a transaction
     *  STEP 0: Prepare all information needed for the transaction
     *  STEP 1: Calculate fee needed for transaction
     *  STEP 1.1: Check free fee supported or not
     *  STEP 2: Check if relay account has already had enough balance to cover transaction fee
     *  STEP 2.1: If relay account has not been created or has not have enough balance, do top up
     *  STEP 2.1.1: Top up with needed amount
     *  STEP 2.1.2: Make transaction
     *  STEP 2.2: Else, skip top up
     *  STEP 2.2.1: Make transaction
     *
     *  @return: Array of strings contain transactions' signatures
     * */

    /*
    * STEP 0: Prepare all information needed for the transaction
    * Load all needed info for relay operations, need to be completed before any operation
    * */
    suspend fun load() = withContext(dispatchers.io) {
        feeRelayerAccountInteractor.getRelayInfo()
        feeRelayerAccountInteractor.getUserRelayAccount(useCache = false)
        feeRelayerAccountInteractor.getFreeTransactionFeeLimit(useCache = false)
    }

    // Calculate needed fee (count in payingToken)
    suspend fun calculateFeeInPayingToken(
        feeInSOL: FeeAmount,
        payingFeeTokenMint: String
    ): FeePoolsState {
        val tradableTopUpPoolsPair = try {
            orcaPoolInteractor.getTradablePoolsPairs(payingFeeTokenMint, WRAPPED_SOL_MINT)
        } catch (e: Throwable) {
            return FeePoolsState.Failed(feeInSOL)
        }
        val topUpPools = try {
            orcaPoolInteractor.findBestPoolsPairForEstimatedAmount(feeInSOL.total, tradableTopUpPoolsPair)
        } catch (e: Throwable) {
            return FeePoolsState.Failed(feeInSOL)
        }

        if (topUpPools.isNullOrEmpty()) {
            return FeePoolsState.Failed(feeInSOL)
        }

        val transactionFee = topUpPools.getInputAmount(
            minimumAmountOut = feeInSOL.transaction,
            slippage = Slippage.TopUpSlippage.doubleValue
        )
        val accountCreationFee = topUpPools.getInputAmount(
            minimumAmountOut = feeInSOL.accountBalances,
            slippage = Slippage.TopUpSlippage.doubleValue
        )

        val fee = FeeAmount(transactionFee.orZero(), accountCreationFee.orZero())
        return FeePoolsState.Calculated(fee)
    }

    /*
    * Generic function for sending transaction to fee relayer's relay
    * */
    suspend fun topUpAndRelayTransaction(
        preparedTransaction: PreparedTransaction,
        payingFeeToken: TokenAccount,
        additionalPaybackFee: BigInteger,
        statistics: FeeRelayerStatistics
    ): List<String> {
        checkAndTopUp(
            expectedFee = preparedTransaction.expectedFee,
            payingFeeToken = payingFeeToken
        )

        return relayTransaction(
            preparedTransaction = preparedTransaction,
            payingFeeToken = payingFeeToken,
            additionalPaybackFee = additionalPaybackFee,
            statistics = statistics
        )
    }

    private suspend fun checkAndTopUp(
        expectedFee: FeeAmount,
        payingFeeToken: TokenAccount
    ): List<String>? {
        if (payingFeeToken.isSOL) return null

        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val info = feeRelayerAccountInteractor.getRelayInfo()

        // Check fee
        if (freeTransactionFeeLimit.isFreeTransactionFeeAvailable(expectedFee.transaction)) {
            expectedFee.transaction = BigInteger.ZERO
        }

        val minRelayAccountBalance = relayAccount.getMinRemainingBalance(info.minimumRelayAccountRent)
        val topUpParams = if (expectedFee.total.isNotZero() && minRelayAccountBalance < expectedFee.total) {
            val topUpAmount = expectedFee.total - minRelayAccountBalance
            feeRelayerTopUpInteractor.prepareForTopUp(
                topUpAmount = topUpAmount,
                payingFeeToken = payingFeeToken,
                relayAccount = relayAccount,
                freeTransactionFeeLimit = freeTransactionFeeLimit
            )
        } else {
            null
        }

        if (topUpParams == null || topUpParams.amount.isZero()) return null

        /*
        * If amount is too low, we may receive [Slippage Exceeded Error],
        * that's why we are topping up min amount
        * */
        val targetAmount = if (topUpParams.amount.isLessThan(MIN_TOP_UP_AMOUNT)) {
            MIN_TOP_UP_AMOUNT
        } else {
            topUpParams.amount
        }

        return feeRelayerTopUpInteractor.topUp(
            feeRelayerProgramId = feeRelayerProgramId,
            needsCreateUserRelayAddress = !relayAccount.isCreated,
            sourceToken = payingFeeToken,
            targetAmount = targetAmount,
            topUpPools = topUpParams.poolsPair,
            expectedFee = topUpParams.expectedFee
        )
    }

    private suspend fun relayTransaction(
        preparedTransaction: PreparedTransaction,
        payingFeeToken: TokenAccount,
        additionalPaybackFee: BigInteger,
        statistics: FeeRelayerStatistics
    ): List<String> {
        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val feePayer = info.feePayerAddress
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount(useCache = false)

        // verify fee payer
        val transactionFeePayer = preparedTransaction.transaction.getFeePayer()
        if (transactionFeePayer == null || !feePayer.equals(transactionFeePayer)) {
            throw IllegalStateException("Invalid fee payer")
        }

        // Calculate the fee to send back to feePayer
        // Account creation fee (accountBalances) is a must-pay-back fee
        var paybackFee = additionalPaybackFee + preparedTransaction.expectedFee.accountBalances

        // The transaction fee, on the other hand, is only be paid if user used more than number of free transaction fee
        if (!freeTransactionFeeLimit.isFreeTransactionFeeAvailable(preparedTransaction.expectedFee.transaction)) {
            paybackFee += preparedTransaction.expectedFee.transaction
        }

        // transfer sol back to feerelayer's feePayer
        val owner = Account(tokenKeyProvider.keyPair)
        val transaction = preparedTransaction.transaction
        if (paybackFee.isNotZero()) {
            val minRelayAccountBalance = relayAccount.getMinRemainingBalance(info.minimumRelayAccountRent)
            if (payingFeeToken.isSOL && minRelayAccountBalance < paybackFee) {
                val instruction = SystemProgram.transfer(
                    fromPublicKey = owner.publicKey,
                    toPublicKey = feePayer,
                    lamports = paybackFee
                )
                transaction.addInstruction(instruction)
            } else {
                val createRelayTransferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
                    programId = feeRelayerProgramId,
                    userAuthority = owner.publicKey,
                    userRelayAccount = relayAccount.publicKey,
                    recipient = feePayer,
                    amount = paybackFee
                )
                transaction.addInstruction(createRelayTransferSolInstruction)
            }
        }

        // resign transaction
        transaction.sign(preparedTransaction.signers)

        /*
        * Retrying 3 times to avoid some errors
        * For example: fee relayer balance is not updated yet and request will fail with insufficient balance error
        * */
        return retryRequest {
            feeRelayerRepository.relayTransaction(transaction, statistics)
        }
    }

    suspend fun relayTransactionWithoutPayback(
        preparedTransaction: PreparedTransaction,
        statistics: FeeRelayerStatistics
    ): String {
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val feePayer = info.feePayerAddress

        // verify fee payer
        val transactionFeePayer = preparedTransaction.transaction.getFeePayer()
        if (transactionFeePayer == null || !feePayer.equals(transactionFeePayer)) {
            throw IllegalStateException("Invalid fee payer")
        }

        /*
        * Retrying 3 times to avoid some errors
        * For example: fee relayer balance is not updated yet and request will fail with insufficient balance error
        * */
        return retryRequest {
            feeRelayerRepository.relayTransaction(preparedTransaction.transaction, statistics).firstOrNull().orEmpty()
        }
    }
}
