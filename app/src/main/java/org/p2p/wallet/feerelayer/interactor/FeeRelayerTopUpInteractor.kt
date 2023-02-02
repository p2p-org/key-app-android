package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.OperationType
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TokenAccount
import org.p2p.wallet.feerelayer.model.TopUpPreparedParams
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.core.utils.isZero
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerTopUpInteractor(
    private val rpcRepository: RpcBlockhashRepository,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    // Submits a signed top up swap transaction to the backend for processing
    suspend fun topUp(
        feeRelayerProgramId: PublicKey,
        needsCreateUserRelayAddress: Boolean,
        sourceToken: TokenAccount,
        targetAmount: BigInteger,
        topUpPools: OrcaPoolsPair,
        expectedFee: BigInteger,
    ): List<String> {
        val blockhash = rpcRepository.getRecentBlockhash()
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val owner = Account(tokenKeyProvider.keyPair)

        // STEP 3: prepare for topUp
        val (swapData, topUpTransaction) = prepareForTopUp(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = sourceToken,
            userAuthorityAddress = owner.publicKey,
            relayAccountPublicKey = relayAccount.publicKey,
            topUpPools = topUpPools,
            targetAmount = targetAmount,
            expectedFee = expectedFee,
            blockhash = blockhash.recentBlockhash,
            minimumRelayAccountBalance = info.minimumRelayAccountRent,
            minimumTokenAccountBalance = info.minimumTokenAccountRent,
            needsCreateUserRelayAccount = needsCreateUserRelayAddress,
            feePayerAddress = info.feePayerAddress.toBase58(),
            lamportsPerSignature = info.lamportsPerSignature
        )

        // STEP 4: send transaction
        val signatures = topUpTransaction.transaction.allSignatures
        if (signatures.size < 1 || signatures[0].signature.isNullOrEmpty()) {
            throw IllegalStateException("Invalid signature")
        }

        // the second signature is the owner's signature
        val ownerSignature = signatures[0].signature!!

        val topUpSignatures = SwapTransactionSignatures(
            userAuthoritySignature = ownerSignature,
            transferAuthoritySignature = ownerSignature
        )

        val statistics = FeeRelayerStatistics(
            operationType = OperationType.TOP_UP,
            currency = sourceToken.mint
        )

        return feeRelayerRepository.relayTopUpSwap(
            userSourceTokenAccountPubkey = sourceToken.address,
            sourceTokenMintPubkey = sourceToken.mint,
            userAuthorityPubkey = owner.publicKey.toBase58(),
            swapData = swapData,
            feeAmount = expectedFee,
            signatures = topUpSignatures,
            blockhash = blockhash.recentBlockhash,
            info = statistics
        )
    }

    suspend fun prepareForTopUp(
        topUpAmount: BigInteger,
        payingFeeToken: TokenAccount,
        relayAccount: RelayAccount,
        freeTransactionFeeLimit: FreeTransactionFeeLimit?
    ): TopUpPreparedParams {

        val tradableTopUpPoolsPair = orcaPoolInteractor.getTradablePoolsPairs(payingFeeToken.mint, WRAPPED_SOL_MINT)

        // Get fee
        val expectedFee = calculateExpectedFeeForTopUp(relayAccount, freeTransactionFeeLimit)

        // Get pools for topping up
        // prefer direct swap to transitive swap
        val directSwapPools = tradableTopUpPoolsPair.firstOrNull { it.size == 1 }
        val topUpPools = if (directSwapPools != null) {
            directSwapPools
        } else {
            // if direct swap is not available, use transitive swap
            val transitiveSwapPools = orcaPoolInteractor.findBestPoolsPairForEstimatedAmount(
                estimatedAmount = topUpAmount,
                poolsPairs = tradableTopUpPoolsPair
            )
            transitiveSwapPools
        }

        if (topUpPools.isNullOrEmpty()) throw IllegalStateException("Swap pools not found")

        return TopUpPreparedParams(
            amount = topUpAmount,
            expectedFee = expectedFee,
            poolsPair = topUpPools
        )
    }

    // Calculate needed top up amount for expected fee
    suspend fun calculateNeededTopUpAmount(expectedFee: FeeAmount): FeeAmount {
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val neededAmount = FeeAmount(expectedFee.transaction, expectedFee.accountBalances)

        // expected fees
        val expectedTopUpNetworkFee = BigInteger.valueOf(2L) * info.lamportsPerSignature
        val expectedTransactionNetworkFee = expectedFee.transaction

        // real fees
        var neededTopUpNetworkFee = expectedTopUpNetworkFee
        var neededTransactionNetworkFee = expectedTransactionNetworkFee

        // is Top up free
        if (freeTransactionFeeLimit.isFreeTransactionFeeAvailable(expectedTopUpNetworkFee)) {
            neededTopUpNetworkFee = BigInteger.ZERO
        }

        // is transaction free
        val freeTransactionFeeAvailable = freeTransactionFeeLimit.isFreeTransactionFeeAvailable(
            transactionFee = expectedTopUpNetworkFee + expectedTransactionNetworkFee,
            forNextTransaction = true
        )
        if (freeTransactionFeeAvailable) {
            neededTransactionNetworkFee = BigInteger.ZERO
        }

        neededAmount.transaction = neededTopUpNetworkFee + neededTransactionNetworkFee

        // transaction is totally free
        if (neededAmount.total.isZero()) {
            return neededAmount
        }

        if (neededAmount.transaction.isZero() && neededAmount.accountBalances.isZero()) {
            return neededAmount
        }

        val minimumRelayAccountBalance = info.minimumRelayAccountRent

        // check if relay account current balance can cover part of needed amount
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        var relayAccountBalance = relayAccount.balance

        if (relayAccountBalance != null) {

            if (relayAccountBalance < minimumRelayAccountBalance) {
                neededAmount.transaction += minimumRelayAccountBalance - relayAccountBalance
            } else {
                relayAccountBalance -= minimumRelayAccountBalance

                // if relayAccountBalance has enough balance to cover transaction fee
                if (relayAccountBalance >= neededAmount.transaction) {
                    neededAmount.transaction = BigInteger.ZERO

                    // if relayAccountBalance has enough balance to cover accountBalances fee too
                    if (relayAccountBalance - neededAmount.transaction >= neededAmount.accountBalances) {
                        neededAmount.accountBalances = BigInteger.ZERO
                    } else {
                        // Relay account balance can cover part of account creation fee
                        neededAmount.accountBalances -= (relayAccountBalance - neededAmount.transaction)
                    }
                } else {
                    // if not, relayAccountBalance can cover part of transaction fee
                    neededAmount.transaction -= relayAccountBalance
                }
            }
        } else {
            neededAmount.accountBalances += minimumRelayAccountBalance
        }
        return neededAmount
    }

    suspend fun calculateExpectedFeeForTopUp(
        relayAccount: RelayAccount,
        freeTransactionFeeLimit: FreeTransactionFeeLimit?
    ): BigInteger {
        val info = feeRelayerAccountInteractor.getRelayInfo()

        var expectedFee: BigInteger = BigInteger.ZERO
        if (!relayAccount.isCreated) {
            expectedFee += info.minimumRelayAccountRent
        }

        val transactionNetworkFee = BigInteger.valueOf(2) * info.lamportsPerSignature // feePayer, owner
        if (freeTransactionFeeLimit?.isFreeTransactionFeeAvailable(transactionNetworkFee) == false) {
            expectedFee += transactionNetworkFee
        }

        expectedFee += info.minimumTokenAccountRent

        return expectedFee
    }

    /*
   * Prepare transaction and expected fee for a given relay transaction
   * */
    private suspend fun prepareForTopUp(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenAccount,
        userAuthorityAddress: PublicKey,
        relayAccountPublicKey: PublicKey,
        topUpPools: OrcaPoolsPair,
        targetAmount: BigInteger,
        expectedFee: BigInteger,
        blockhash: String,
        minimumRelayAccountBalance: BigInteger,
        minimumTokenAccountBalance: BigInteger,
        needsCreateUserRelayAccount: Boolean,
        feePayerAddress: String,
        lamportsPerSignature: BigInteger
    ): Pair<SwapData, PreparedTransaction> {

        // assertion
        val userSourceTokenAccountAddress = PublicKey(sourceToken.address)
        val sourceTokenMintAddress = PublicKey(sourceToken.mint)
        val feePayerAddressPublicKey = PublicKey(feePayerAddress)
        val associatedTokenAddress = TokenTransaction.getAssociatedTokenAddress(
            owner = feePayerAddressPublicKey,
            mint = sourceTokenMintAddress
        )
        if (userSourceTokenAccountAddress == associatedTokenAddress) {
            throw IllegalStateException("Wrong address")
        }

        // forming transaction and count fees
        var accountCreationFee: BigInteger = BigInteger.ZERO
        val instructions = mutableListOf<TransactionInstruction>()

        // create user relay account
        if (needsCreateUserRelayAccount) {
            val transferInstruction = SystemProgram.transfer(
                fromPublicKey = feePayerAddress.toPublicKey(),
                toPublicKey = relayAccountPublicKey,
                lamports = minimumRelayAccountBalance
            )
            instructions += transferInstruction

            accountCreationFee += minimumRelayAccountBalance
        }

        // top up swap
        val transitTokenMintPubKey = feeRelayerInstructionsInteractor.getTransitTokenMintPubkey(topUpPools)
        if (transitTokenMintPubKey == null && topUpPools.size > 1) {
            error("Transit mint is null")
        }
        val topUpSwap = feeRelayerInstructionsInteractor.prepareSwapData(
            pools = topUpPools,
            inputAmount = null,
            minAmountOut = targetAmount,
            slippage = Slippage.TopUpSlippage.doubleValue,
            transitTokenMintPubkey = transitTokenMintPubKey,
            userAuthorityAddress = userAuthorityAddress,
        )

        val userTemporarilyWSOLAddress = feeRelayerAccountInteractor.getUserTemporaryWsolAccount(userAuthorityAddress)
        val foundUserRelayAddress = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress)
        when (topUpSwap) {
            is SwapData.Direct -> {
                accountCreationFee += minimumTokenAccountBalance

                // top up
                val topUpSwapInstruction = FeeRelayerProgram.topUpSwapInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    userRelayAddress = foundUserRelayAddress,
                    userTemporarilyWSOLAddress = userTemporarilyWSOLAddress,
                    topUpSwap = topUpSwap,
                    userAuthorityAddress = userAuthorityAddress,
                    userSourceTokenAccountAddress = userSourceTokenAccountAddress,
                    feePayerAddress = feePayerAddress.toPublicKey(),
                )
                instructions += topUpSwapInstruction
            }
            is SwapData.SplTransitive -> {

                // create transit token account
                val transitTokenMint = topUpSwap.transitTokenMintPubkey.toPublicKey()
                if (topUpSwap.needsCreateTransitTokenAccount) {
                    val createTransitInstruction = FeeRelayerProgram.createTransitTokenAccountInstruction(
                        programId = feeRelayerProgramId,
                        feePayer = feePayerAddress.toPublicKey(),
                        userAuthority = userAuthorityAddress,
                        transitTokenAccount = topUpSwap.transitTokenAccountAddress,
                        transitTokenMint = transitTokenMint,
                    )
                    instructions += createTransitInstruction
                }

                // Destination WSOL account funding
                accountCreationFee += minimumTokenAccountBalance

                val topUpSwapInstruction = FeeRelayerProgram.topUpSwapInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    userRelayAddress = foundUserRelayAddress,
                    userTemporarilyWSOLAddress = userTemporarilyWSOLAddress,
                    topUpSwap = topUpSwap,
                    userAuthorityAddress = userAuthorityAddress,
                    userSourceTokenAccountAddress = userSourceTokenAccountAddress,
                    feePayerAddress = feePayerAddress.toPublicKey(),
                )
                instructions += topUpSwapInstruction
            }
        }

        // transfer
        val transferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
            programId = feeRelayerProgramId,
            userAuthority = userAuthorityAddress,
            userRelayAccount = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress),
            recipient = feePayerAddress.toPublicKey(),
            amount = expectedFee
        )
        instructions += transferSolInstruction

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.feePayer = feePayerAddress.toPublicKey()
        transaction.recentBlockHash = blockhash

        // calculate fee first
        val expectedFeeAmount = FeeAmount(
            transaction = transaction.calculateTransactionFee(lamportsPerSignature),
            accountBalances = accountCreationFee
        )

        // resign transaction
        val owner = Account(tokenKeyProvider.keyPair)
        val signers = mutableListOf(owner)

        transaction.sign(signers)

        return topUpSwap to PreparedTransaction(transaction, signers, expectedFeeAmount)
    }
}
