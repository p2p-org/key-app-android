package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.AccountMeta
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Signature
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.model.TopUpPreparedParams
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerTopUpInteractor(
    private val rpcRepository: RpcRepository,
    private val orcaSwapInteractor: OrcaSwapInteractor,
    private val feeRelayerRepository: FeeRelayerRepository,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    // Submits a signed top up swap transaction to the backend for processing
    suspend fun topUp(
        feeRelayerProgramId: PublicKey,
        needsCreateUserRelayAddress: Boolean,
        sourceToken: TokenInfo,
        targetAmount: BigInteger,
        topUpPools: OrcaPoolsPair,
        expectedFee: BigInteger
    ): List<String> {
        val blockhash = rpcRepository.getRecentBlockhash()
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val owner = Account(tokenKeyProvider.secretKey)

        // STEP 3: prepare for topUp
        val (swapData, topUpTransaction) = prepareForTopUp(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = sourceToken,
            userAuthorityAddress = owner.publicKey,
            userRelayAddress = relayAccount.publicKey,
            topUpPools = topUpPools,
            targetAmount = targetAmount,
            expectedFee = expectedFee,
            blockhash = blockhash.recentBlockhash,
            minimumRelayAccountBalance = info.minimumRelayAccountBalance,
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            needsCreateUserRelayAccount = needsCreateUserRelayAddress,
            feePayerAddress = info.feePayerAddress.toBase58(),
            lamportsPerSignature = info.lamportsPerSignature
        )

        // STEP 4: send transaction
        val signatures = topUpTransaction.transaction.allSignatures
        if (signatures.size < 2) {
            throw IllegalStateException("Invalid signature")
        }

        // the second signature is the owner's signature
        val ownerSignature = signatures[1].signature

        // the third signature (optional) is the transferAuthority's signature
        val transferAuthoritySignature = signatures.getOrNull(2)?.signature

        val topUpSignatures = SwapTransactionSignatures(
            userAuthoritySignature = ownerSignature,
            transferAuthoritySignature = transferAuthoritySignature
        )

        return feeRelayerRepository.relayTopUpSwap(
            userSourceTokenAccountPubkey = sourceToken.address,
            sourceTokenMintPubkey = sourceToken.mint,
            userAuthorityPubkey = owner.publicKey.toBase58(),
            swapData = swapData,
            feeAmount = expectedFee,
            signatures = topUpSignatures,
            blockhash = blockhash.recentBlockhash
        )
    }

    suspend fun prepareForTopUp(
        targetAmount: BigInteger,
        payingFeeToken: TokenInfo,
        relayAccount: RelayAccount,
        freeTransactionFeeLimit: FreeTransactionFeeLimit?,
        checkIfBalanceHaveEnoughAmount: Boolean = true
    ): TopUpPreparedParams? {

        val tradableTopUpPoolsPair = orcaSwapInteractor.getTradablePoolsPairs(payingFeeToken.mint, WRAPPED_SOL_MINT)
        // TOP UP
        if (checkIfBalanceHaveEnoughAmount && (relayAccount.balance != null && relayAccount.balance >= targetAmount)) {
            return null
        } else {
            // STEP 2.2: Else

            // Get target amount for topping up
            var targetAmount = targetAmount
            if (checkIfBalanceHaveEnoughAmount) {
                targetAmount -= (relayAccount.balance ?: BigInteger.ZERO)
            }

            // Get real amounts needed for topping up
            val (topUpAmount, expectedFee) = calculateTopUpAmount(
                targetAmount = targetAmount,
                relayAccount = relayAccount,
                freeTransactionFeeLimit = freeTransactionFeeLimit
            )

            // Get pools for topping up
            // Get pools
            var topUpPools: OrcaPoolsPair? = null

            // TODO: - Temporary solution, prefer direct swap to transitive swap to omit error Non-zero account can only be close if balance zero
            val directSwapPools = tradableTopUpPoolsPair.firstOrNull { it.size == 1 }
            if (directSwapPools != null) {
                topUpPools = directSwapPools
            } else {
                val transitiveSwapPools =
                    orcaSwapInteractor.findBestPoolsPairForEstimatedAmount(topUpAmount, tradableTopUpPoolsPair)
                if (!transitiveSwapPools.isNullOrEmpty()) {
                    topUpPools = transitiveSwapPools
                }
            }

            if (topUpPools.isNullOrEmpty()) throw IllegalStateException("Swap pools not found")

            return TopUpPreparedParams(
                amount = topUpAmount,
                expectedFee = expectedFee,
                poolsPair = topUpPools
            )
        }
    }

    /*
   * Calculate needed fee for topup transaction by forming fake transaction
   * */
    fun calculateTopUpFee(
        info: RelayInfo,
        relayAccount: RelayAccount
    ): FeeAmount {
        val topUpFee = FeeAmount()

        // transaction fee
        val numberOfSignatures: BigInteger = BigInteger.valueOf(2) // feePayer's signature, owner's Signature
//        numberOfSignatures += 1 // transferAuthority
        topUpFee.transaction = numberOfSignatures * info.lamportsPerSignature

        // account creation fee
        if (!relayAccount.isCreated) {
            topUpFee.accountBalances += info.minimumRelayAccountBalance
        }

        // swap fee
        topUpFee.accountBalances += info.minimumTokenAccountBalance

        return topUpFee
    }

    suspend fun calculateTopUpAmount(
        targetAmount: BigInteger,
        relayAccount: RelayAccount,
        freeTransactionFeeLimit: FreeTransactionFeeLimit?
    ): Pair<BigInteger, BigInteger> {
        val info = feeRelayerAccountInteractor.getRelayInfo()
        // current_fee
        var currentFee: BigInteger = BigInteger.ZERO
        if (!relayAccount.isCreated) {
            currentFee += info.minimumRelayAccountBalance
        }

        val transactionNetworkFee = BigInteger.valueOf(2) * info.lamportsPerSignature // feePayer, owner
        if (freeTransactionFeeLimit?.isFreeTransactionFeeAvailable(transactionNetworkFee) == false) {
            currentFee += transactionNetworkFee
        }

        // swap_amount_out
//        let swapAmountOut = targetAmount + currentFee
        var swapAmountOut = targetAmount
        if (!relayAccount.isCreated) {
            swapAmountOut += info.lamportsPerSignature // Temporary solution
        } else {
            swapAmountOut += currentFee
        }

        // expected_fee
        val expectedFee = currentFee + info.minimumTokenAccountBalance

        return swapAmountOut to expectedFee
    }

    /*
   * Prepare transaction and expected fee for a given relay transaction
   * */
    private fun prepareForTopUp(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        userAuthorityAddress: PublicKey,
        userRelayAddress: PublicKey,
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
                toPublicKey = userRelayAddress,
                lamports = minimumTokenAccountBalance
            )
            instructions += transferInstruction

            accountCreationFee += minimumRelayAccountBalance
        }

        // top up swap
        val transitTokenMintPubkey = feeRelayerInstructionsInteractor.getTransitTokenMintPubkey(topUpPools)
        val (topUpSwap, transferAuthorityAccount) = feeRelayerInstructionsInteractor.prepareSwapData(
            pools = topUpPools,
            inputAmount = null,
            minAmountOut = targetAmount,
            slippage = Slippage.PERCENT.doubleValue,
            transitTokenMintPubkey = transitTokenMintPubkey,
            userAuthorityAddress = userAuthorityAddress,
        )
        val userTransferAuthority = transferAuthorityAccount?.publicKey

        val userTemporarilyWSOLAddress = feeRelayerAccountInteractor.getUserTemporaryWsolAccount(userAuthorityAddress)
        val userRelayAddress = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress)
        when (topUpSwap) {
            is SwapData.Direct -> {
                accountCreationFee += minimumTokenAccountBalance

                if (userTransferAuthority != null) {
                    // approve
                    val approveInstruction = TokenProgram.approveInstruction(
                        TokenProgram.PROGRAM_ID,
                        userSourceTokenAccountAddress,
                        userTransferAuthority,
                        userAuthorityAddress,
                        topUpSwap.amountIn
                    )
                    instructions += approveInstruction
                }

                // top up
                val topUpSwapInstruction = FeeRelayerProgram.topUpSwapInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    userRelayAddress = userRelayAddress,
                    userTemporarilyWSOLAddress = userTemporarilyWSOLAddress,
                    topUpSwap = topUpSwap,
                    userAuthorityAddress = userAuthorityAddress,
                    userSourceTokenAccountAddress = userSourceTokenAccountAddress,
                    feePayerAddress = feePayerAddress.toPublicKey(),
                )
                instructions += topUpSwapInstruction
            }
            is SwapData.SplTransitive -> {
                // approve
                if (userTransferAuthority != null) {
                    val approveInstruction = TokenProgram.approveInstruction(
                        TokenProgram.PROGRAM_ID,
                        userSourceTokenAccountAddress,
                        topUpSwap.from.transferAuthorityPubkey.toPublicKey(),
                        userAuthorityAddress,
                        topUpSwap.from.amountIn
                    )
                    instructions += approveInstruction
                }

                // create transit token account
                val transitTokenMint = topUpSwap.transitTokenMintPubkey.toPublicKey()
                val transitTokenAccountAddress = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                    owner = userAuthorityAddress,
                    mint = transitTokenMint
                )

                val createTransitInstruction = FeeRelayerProgram.createTransitTokenAccountInstruction(
                    programId = feeRelayerProgramId,
                    feePayer = feePayerAddress.toPublicKey(),
                    userAuthority = userAuthorityAddress,
                    transitTokenAccount = transitTokenAccountAddress,
                    transitTokenMint = transitTokenMint,
                )
                instructions += createTransitInstruction

                // Destination WSOL account funding
                accountCreationFee += minimumTokenAccountBalance

                val topUpSwapInstruction = FeeRelayerProgram.topUpSwapInstruction(
                    feeRelayerProgramId = feeRelayerProgramId,
                    userRelayAddress = userRelayAddress,
                    userTemporarilyWSOLAddress = userTemporarilyWSOLAddress,
                    topUpSwap = topUpSwap,
                    userAuthorityAddress = userAuthorityAddress,
                    userSourceTokenAccountAddress = userSourceTokenAccountAddress,
                    feePayerAddress = feePayerAddress.toPublicKey(),
                )
                instructions += topUpSwapInstruction

                // close transit token account
                val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    transitTokenAccountAddress,
                    feePayerAddress.toPublicKey(),
                    feePayerAddress.toPublicKey(),
                )
                instructions += closeAccountInstruction
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
        val expectedFee = FeeAmount(
            transaction = transaction.calculateTransactionFee(lamportsPerSignature),
            accountBalances = accountCreationFee
        )

        // resign transaction
        val owner = Account(tokenKeyProvider.secretKey)
        val signers = mutableListOf(owner)

        if (transferAuthorityAccount != null) {
            signers.add(0, transferAuthorityAccount)
        }

        transaction.sign(signers)

        return topUpSwap to PreparedTransaction(transaction, signers, expectedFee)
    }

    suspend fun relayTransaction(
        instructions: List<TransactionInstruction>,
        signatures: List<Signature>,
        pubkeys: List<AccountMeta>,
        blockHash: String
    ): List<String> =
        feeRelayerRepository.relayTransaction(instructions, signatures, pubkeys, blockHash)
}