package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.isLessThan
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.isZero
import org.p2p.wallet.utils.orZero
import org.p2p.wallet.utils.retryRequest
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeeRelayerInteractor(
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager
) {

    companion object {
        private val MIN_TOP_UP_AMOUNT = BigInteger.valueOf(1000L)
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
    suspend fun load() = withContext(Dispatchers.IO) {
        feeRelayerAccountInteractor.getRelayInfo()
        feeRelayerAccountInteractor.getUserRelayAccount(reuseCache = false)
        feeRelayerAccountInteractor.getFreeTransactionFeeLimit(useCache = false)
    }

    // Calculate needed fee (count in payingToken)
    suspend fun calculateFeeInPayingToken(
        feeInSOL: FeeAmount,
        payingFeeTokenMint: String
    ): FeeAmount {
        val tradableTopUpPoolsPair = orcaPoolInteractor.getTradablePoolsPairs(payingFeeTokenMint, WRAPPED_SOL_MINT)
        val topUpPools = orcaPoolInteractor.findBestPoolsPairForEstimatedAmount(feeInSOL.total, tradableTopUpPoolsPair)

        if (topUpPools.isNullOrEmpty()) {
            throw IllegalStateException("Swap pools not found")
        }

        val transactionFee = topUpPools.getInputAmount(
            minimumAmountOut = feeInSOL.transaction,
            slippage = Slippage.Percent.doubleValue
        )
        val accountCreationFee = topUpPools.getInputAmount(
            minimumAmountOut = feeInSOL.accountBalances,
            slippage = Slippage.Percent.doubleValue
        )

        return FeeAmount(transactionFee ?: BigInteger.ZERO, accountCreationFee ?: BigInteger.ZERO)
    }

    /*
    * Generic function for sending transaction to fee relayer's relay
    * */
    suspend fun topUpAndRelayTransaction(
        preparedTransactions: List<PreparedTransaction>,
        payingFeeToken: TokenInfo,
        additionalPaybackFee: BigInteger,
    ): List<String> {
        val expectedFee = FeeAmount(
            transaction = preparedTransactions
                .map { it.expectedFee.transaction }
                .fold(BigInteger.ZERO, BigInteger::add),
            accountBalances = preparedTransactions
                .map { it.expectedFee.accountBalances }
                .fold(BigInteger.ZERO, BigInteger::add),
        )
        checkAndTopUp(
            expectedFee = expectedFee,
            payingFeeToken = payingFeeToken
        )

        if (preparedTransactions.isEmpty()) throw IllegalStateException("Transactions cannot be empty!")
        val transactionId = if (preparedTransactions.size > 1) {
            relayTransaction(
                preparedTransaction = preparedTransactions.first(),
                payingFeeToken = payingFeeToken,
                additionalPaybackFee = additionalPaybackFee
            )

            relayTransaction(
                preparedTransaction = preparedTransactions[1],
                payingFeeToken = payingFeeToken,
                additionalPaybackFee = additionalPaybackFee
            )
        } else {
            relayTransaction(
                preparedTransaction = preparedTransactions.first(),
                payingFeeToken = payingFeeToken,
                additionalPaybackFee = additionalPaybackFee
            )
        }

        return transactionId
    }

    private suspend fun checkAndTopUp(
        expectedFee: FeeAmount,
        payingFeeToken: TokenInfo
    ): List<String>? {
        if (payingFeeToken.isSOL) return null

        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

        // Check fee
        if (freeTransactionFeeLimit.isFreeTransactionFeeAvailable(expectedFee.transaction)) {
            expectedFee.transaction = BigInteger.ZERO
        }

        // if payingFeeToken is provided
        val topUpParams = when {
            expectedFee.total.isNotZero() -> {
                feeRelayerTopUpInteractor.prepareForTopUp(
                    topUpAmount = expectedFee.total,
                    payingFeeToken = payingFeeToken,
                    relayAccount = relayAccount,
                    freeTransactionFeeLimit = freeTransactionFeeLimit
                )
            }

            // if not, make sure that relayAccountBalance is greater or equal to expected fee
            relayAccount.balance.orZero() >= expectedFee.total -> {
                // skip topup
                return null
            }

            // fee paying token is required but missing
            else -> {
                throw IllegalStateException("Fee paying token is missing")
            }
        }

        if (topUpParams.amount.isZero()) return null

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
        payingFeeToken: TokenInfo,
        additionalPaybackFee: BigInteger
    ): List<String> {
        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val feePayer = info.feePayerAddress
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

        // verify fee payer
        if (!feePayer.equals(preparedTransaction.transaction.feePayer)) {
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
        val owner = Account(tokenKeyProvider.secretKey)
        val transaction = preparedTransaction.transaction
        if (payingFeeToken.isSOL && paybackFee.isNotZero()) {
            if (relayAccount.balance.orZero() < paybackFee) {
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
            feeRelayerRepository.relayTransaction(transaction)
        }
    }
}
