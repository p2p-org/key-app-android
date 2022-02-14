package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.model.TopUpPreparedParams
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import java.math.BigInteger

class FeeRelayerInteractor(
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val environmentManager: EnvironmentManager
) {

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
        feeRelayerAccountInteractor.getUserRelayAccount()
    }

    // Calculate fee for given transaction
    suspend fun calculateFee(preparedTransaction: PreparedTransaction): FeeAmount {
        val fee = preparedTransaction.expectedFee
        // TODO: - Check if free transaction available
        val feeLimits = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val userRelayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val userRelayInfo = feeRelayerAccountInteractor.getRelayInfo()
        if (feeLimits.isFreeTransactionFeeAvailable(fee.transaction)) {
            fee.transaction = BigInteger.ZERO
        } else {
            if (userRelayAccount.isCreated) {
                fee.transaction += userRelayInfo.lamportsPerSignature // TODO: - accountBalances or transaction?
            } else {
                fee.transaction += BigInteger.valueOf(2) * (userRelayInfo.lamportsPerSignature) // Top up network fee
            }
        }

        return fee
    }

    /*
    *  Generic function for sending transaction to fee relayer's relay
    * */
    suspend fun topUpAndRelayTransaction(
        preparedTransaction: PreparedTransaction,
        payingFeeToken: TokenInfo?
    ): List<String> {
        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())

        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

        // Check fee
        val expectedFee = preparedTransaction.expectedFee
        if (freeTransactionFeeLimit.isFreeTransactionFeeAvailable(expectedFee.transaction)) {
            expectedFee.transaction = BigInteger.ZERO
        }

        var params: TopUpPreparedParams? = null
        // if payingFeeToken is provided
        if (payingFeeToken != null) {
            params = feeRelayerTopUpInteractor.prepareForTopUp(
                targetAmount = expectedFee.total,
                payingFeeToken = payingFeeToken,
                relayAccount = relayAccount,
                freeTransactionFeeLimit = freeTransactionFeeLimit
            )
        }

        // if not, make sure that relayAccountBalance is greater or equal to expected fee
        if ((relayAccount.balance ?: BigInteger.ZERO) >= expectedFee.total) {
            // skip topup
            params = null
        }

        val feePayer = info.feePayerAddress
        // verify fee payer
        if (!feePayer.equals(preparedTransaction.transaction.feePayer)) {
            throw IllegalStateException("Invalid fee payer")
        }

        // Calculate the fee to send back to feePayer
        // Account creation fee (accountBalances) is a must-pay-back fee
        var paybackFee = preparedTransaction.expectedFee.accountBalances

        // The transaction fee, on the other hand, is only be paid if user used more than number of free transaction fee
        if (!freeTransactionFeeLimit.isFreeTransactionFeeAvailable(preparedTransaction.expectedFee.transaction)) {
            paybackFee += preparedTransaction.expectedFee.transaction
        }

        // transfer sol back to feerelayer's feePayer
        val owner = Account(tokenKeyProvider.secretKey)
        val transaction = preparedTransaction.transaction
        if (paybackFee > BigInteger.ZERO) {
            val createRelayTransferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
                programId = feeRelayerProgramId,
                userAuthority = owner.publicKey,
                userRelayAccount = feeRelayerAccountInteractor.getUserRelayAddress(owner.publicKey),
                recipient = feePayer,
                amount = paybackFee
            )
            transaction.addInstruction(createRelayTransferSolInstruction)
        }

        // resign transaction
        transaction.sign(preparedTransaction.signers)

        // check if top up is needed
        return if (params == null || payingFeeToken == null) {
            feeRelayerTopUpInteractor.relayTransaction(
                instructions = transaction.instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = transaction.recentBlockHash
            )
        } else {
            // STEP 2.2.1: Top up
            feeRelayerTopUpInteractor.topUp(
                feeRelayerProgramId = feeRelayerProgramId,
                needsCreateUserRelayAddress = !relayAccount.isCreated,
                sourceToken = payingFeeToken,
                targetAmount = params.amount,
                topUpPools = params.poolsPair,
                expectedFee = params.expectedFee
            )

            feeRelayerTopUpInteractor.relayTransaction(
                instructions = transaction.instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = transaction.recentBlockHash
            )
        }
    }
}