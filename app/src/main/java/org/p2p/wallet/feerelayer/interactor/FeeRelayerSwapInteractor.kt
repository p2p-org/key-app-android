package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PreparedTransaction
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.wallet.feerelayer.model.FeesAndPools
import org.p2p.wallet.feerelayer.model.FeesAndTopUpAmount
import org.p2p.wallet.feerelayer.model.FixedDestination
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.model.TopUpAndActionPreparedParams
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.TransactionAddressData
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerSwapInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val addressInteractor: TransactionAddressInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    private var preparedParams: TopUpAndActionPreparedParams? = null

    /*
     * STEP 1: Calculate fee needed for transaction
     * Calculate fee and needed amount for top up and swap
     * Fee calculation is in IN SOL
     * */
    suspend fun calculateFeeAndNeededTopUpAmountForSwapping(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair
    ): FeesAndTopUpAmount {
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()

        val preparedParams = prepareForTopUpAndSwap(
            sourceToken = sourceToken,
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress,
            payingFeeToken = payingFeeToken,
            swapPools = swapPools,
            relayAccount = relayAccount,
            reuseCache = true
        )

        val topUpPools = preparedParams.topUpFeesAndPools?.poolsPair

        val feeAmountInSOL = preparedParams.actionFeesAndPools.fee

        if (!relayAccount.isCreated) {
            feeAmountInSOL.transaction += relayInfo.lamportsPerSignature
        }

        val topUpAmount = preparedParams.topUpAmount

        var feeAmountInPayingToken: FeeAmount? = null
        var topUpAmountInPayingToken: BigInteger? = null

        if (topUpPools != null) {
            val transactionFee =
                topUpPools.getInputAmount(feeAmountInSOL.transaction, Slippage.PERCENT.doubleValue)
            val accountCreationFee =
                topUpPools.getInputAmount(feeAmountInSOL.accountBalances, Slippage.PERCENT.doubleValue)

            if (transactionFee != null && accountCreationFee != null) {
                feeAmountInPayingToken = FeeAmount(transactionFee, accountCreationFee)
            }

            if (topUpAmount != null) {
                topUpAmountInPayingToken = topUpPools.getInputAmount(topUpAmount, Slippage.PERCENT.doubleValue)
            }
        }

        return FeesAndTopUpAmount(
            feeInSOL = feeAmountInSOL,
            topUpAmountInSOL = topUpAmount,
            feeInPayingToken = feeAmountInPayingToken,
            topUpAmountInPayingToken = topUpAmountInPayingToken
        )
    }

    // Calculate needed fee (count in payingToken)
    suspend fun calculateFeeInPayingToken(
        feeInSOL: BigInteger,
        payingFeeToken: TokenInfo
    ): BigInteger? {
        val tradableTopUpPoolsPair = orcaPoolInteractor.getTradablePoolsPairs(payingFeeToken.mint, WRAPPED_SOL_MINT)
        val topUpPools = orcaPoolInteractor.findBestPoolsPairForEstimatedAmount(feeInSOL, tradableTopUpPoolsPair)

        if (topUpPools.isNullOrEmpty()) {
            throw IllegalStateException("Swap pools not found")
        }

        return topUpPools.getInputAmount(feeInSOL, Slippage.PERCENT.doubleValue)
    }

    // Prepare swap transaction for relay
    suspend fun prepareSwapTransaction(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo, // this should not be SOL here
        swapPools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double
    ): PreparedTransaction {
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()

        val info = feeRelayerAccountInteractor.getRelayInfo()

        val preparedParams = prepareForTopUpAndSwap(
            sourceToken = sourceToken,
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress,
            payingFeeToken = payingFeeToken,
            swapPools = swapPools,
            relayAccount = relayAccount,
            reuseCache = false
        )
        val destination = getFixedDestination(destinationTokenMint)

        val recentBlockhash = rpcRepository.getRecentBlockhash()

        val destinationToken = destination.destinationToken
        val userDestinationAccountOwnerAddress = destination.userDestinationAccountOwnerAddress
        val needsCreateDestinationTokenAccount = destination.needsCreateDestinationTokenAccount

        val swapFeesAndPools = preparedParams.actionFeesAndPools
        val swappingFee = swapFeesAndPools.fee.total
        val swapPools = swapFeesAndPools.poolsPair

        return prepareSwapTransaction(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = sourceToken,
            destinationToken = destinationToken,
            pools = swapPools,
            inputAmount = inputAmount,
            slippage = slippage,
            blockhash = recentBlockhash.recentBlockhash,
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount,
            feePayerAddress = info.feePayerAddress,
            lamportsPerSignature = info.lamportsPerSignature
        )
    }

    private fun calculateSwappingFee(
        info: RelayInfo,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        needsCreateDestinationTokenAccount: Boolean
    ): FeeAmount {
        val expectedFee = FeeAmount()

        // fee for payer's signature
        expectedFee.transaction += info.lamportsPerSignature

        // fee for owner's signature
        expectedFee.transaction += info.lamportsPerSignature

        // when source token is native SOL
        if (sourceToken.mint == WRAPPED_SOL_MINT) {
            // WSOL's signature
            expectedFee.transaction += info.lamportsPerSignature

            // TODO: - Account creation fee?
            expectedFee.accountBalances += info.minimumTokenAccountBalance
        }

        // when needed to create destination
        if (needsCreateDestinationTokenAccount && destinationToken.mint != WRAPPED_SOL_MINT) {
            expectedFee.accountBalances += info.minimumTokenAccountBalance
        }

        // when destination is native SOL
        if (destinationToken.mint == WRAPPED_SOL_MINT) {
            expectedFee.transaction += info.lamportsPerSignature
        }

        return expectedFee
    }

    private suspend fun prepareSwapTransaction(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        pools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double,
        blockhash: String,
        minimumTokenAccountBalance: BigInteger,
        needsCreateDestinationTokenAccount: Boolean,
        feePayerAddress: PublicKey,
        lamportsPerSignature: BigInteger
    ): PreparedTransaction {
        val owner = Account(tokenKeyProvider.secretKey)

        val userAuthorityAddress = owner.publicKey

        var userSourceTokenAccountAddress = sourceToken.address.toPublicKey()
        val sourceTokenMintAddress = sourceToken.mint.toPublicKey()
        val associatedTokenAddress = TokenTransaction.getAssociatedTokenAddress(
            mint = sourceTokenMintAddress,
            owner = feePayerAddress
        )
        if (userSourceTokenAccountAddress == associatedTokenAddress) {
            throw IllegalStateException("Wrong address")
        }
        val destinationTokenMintAddress = destinationToken.mint.toPublicKey()

        // forming transaction and count fees
        var accountCreationFee: BigInteger = BigInteger.ZERO
        val instructions = mutableListOf<TransactionInstruction>()

        // check source
        var sourceWSOLNewAccount: Account? = null
        if (sourceToken.mint == WRAPPED_SOL_MINT) {
            sourceWSOLNewAccount = Account()
            val createAccountInstruction = SystemProgram.createAccount(
                feePayerAddress,
                sourceWSOLNewAccount.publicKey,
                (inputAmount + minimumTokenAccountBalance).toLong()
            )

            instructions += createAccountInstruction

            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                sourceWSOLNewAccount.publicKey,
                WRAPPED_SOL_MINT.toPublicKey(),
                userAuthorityAddress
            )
            instructions += initializeAccountInstruction

            userSourceTokenAccountAddress = sourceWSOLNewAccount.publicKey
        }

        // check destination
        var destinationNewAccount: Account? = null
        var userDestinationTokenAccountAddress = destinationToken.address.toPublicKey()
        if (needsCreateDestinationTokenAccount) {
            destinationNewAccount = Account()

            val createAccountInstruction = SystemProgram.createAccount(
                feePayerAddress,
                destinationNewAccount.publicKey,
                minimumTokenAccountBalance.toLong()
            )
            instructions += createAccountInstruction
            val initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                destinationNewAccount.publicKey,
                destinationTokenMintAddress,
                userAuthorityAddress
            )
            instructions += initializeAccountInstruction
            accountCreationFee += minimumTokenAccountBalance
            userDestinationTokenAccountAddress = destinationNewAccount.publicKey
        }

        // swap
        val transitTokenMintPubkey = feeRelayerInstructionsInteractor.getTransitTokenMintPubkey(pools)
        val swapData = feeRelayerInstructionsInteractor.prepareSwapData(
            pools = pools,
            inputAmount = inputAmount,
            minAmountOut = null,
            slippage = slippage,
            transitTokenMintPubkey = transitTokenMintPubkey,
            userAuthorityAddress = userAuthorityAddress
        )
        val userTransferAuthority = Account(tokenKeyProvider.secretKey)

        when (swapData) {
            is SwapData.Direct -> {
                // swap
                val pool = pools.first()
                val swapInstruction = TokenSwapProgram.swapInstruction(
                    pool.account,
                    pool.authority,
                    userTransferAuthority.publicKey,
                    userSourceTokenAccountAddress,
                    pool.tokenAccountA,
                    pool.tokenAccountB,
                    userDestinationTokenAccountAddress,
                    pool.poolTokenMint,
                    pool.feeAccount,
                    pool.hostFeeAccount,
                    TokenProgram.PROGRAM_ID,
                    pool.swapProgramId,
                    swapData.amountIn,
                    swapData.minimumAmountOut
                )

                instructions += swapInstruction
            }
            is SwapData.SplTransitive -> {
                // create transit token account
                val transitTokenAccount = swapData.transitTokenAccountAddress
                if (swapData.needsCreateTransitTokenAccount) {
                    val transitTokenAccountInstruction = FeeRelayerProgram.createTransitTokenAccountInstruction(
                        programId = feeRelayerProgramId,
                        feePayer = feePayerAddress,
                        userAuthority = userAuthorityAddress,
                        transitTokenAccount = transitTokenAccount,
                        transitTokenMint = swapData.transitTokenMintPubkey.toPublicKey()
                    )
                    instructions += transitTokenAccountInstruction
                }
                // Destination WSOL account funding
                accountCreationFee += minimumTokenAccountBalance

                // relay swap
                val relaySwapInstruction = FeeRelayerProgram.createRelaySwapInstruction(
                    programId = feeRelayerProgramId,
                    transitiveSwap = swapData,
                    userAuthorityAddressPubkey = userAuthorityAddress,
                    sourceAddressPubkey = userSourceTokenAccountAddress,
                    transitTokenAccount = transitTokenAccount,
                    destinationAddressPubkey = userDestinationTokenAccountAddress,
                    feePayerPubkey = feePayerAddress,
                )
                instructions += relaySwapInstruction
            }
        }

        // WSOL close
        // close source
        if (sourceWSOLNewAccount != null) {
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                sourceWSOLNewAccount.publicKey,
                userAuthorityAddress,
                userAuthorityAddress
            )
            instructions += closeAccountInstruction

            val transferInstruction = SystemProgram.transfer(
                userAuthorityAddress,
                feePayerAddress,
                minimumTokenAccountBalance
            )
            instructions += transferInstruction
        }

        // close destination
        if (destinationNewAccount != null && destinationTokenMintAddress.toBase58() == WRAPPED_SOL_MINT) {
            val closeAccountInstruction = TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                destinationNewAccount.publicKey,
                userAuthorityAddress,
                userAuthorityAddress
            )

            instructions += closeAccountInstruction

            val transferInstruction = SystemProgram.transfer(
                userAuthorityAddress,
                feePayerAddress,
                minimumTokenAccountBalance
            )
            instructions += transferInstruction
            accountCreationFee -= minimumTokenAccountBalance
        }

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.recentBlockHash = blockhash
        transaction.feePayer = feePayerAddress

        // calculate fee first
        val expectedFee = FeeAmount(
            transaction = transaction.calculateTransactionFee(lamportsPerSignature),
            accountBalances = accountCreationFee
        )

        // resign transaction
        val signers = mutableListOf(owner)
        if (sourceWSOLNewAccount != null) {
            signers += sourceWSOLNewAccount
        }

        if (destinationNewAccount != null) {
            signers += destinationNewAccount
        }
        transaction.sign(signers)

        return PreparedTransaction(transaction, signers, expectedFee)
    }

    private suspend fun prepareForTopUpAndSwap(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair,
        relayAccount: RelayAccount,
        reuseCache: Boolean
    ): TopUpAndActionPreparedParams {

        if (reuseCache && preparedParams != null) {
            return preparedParams!!
        }

        val tradableTopUpPoolsPair = orcaPoolInteractor.getTradablePoolsPairs(
            fromMint = payingFeeToken.mint,
            toMint = WRAPPED_SOL_MINT
        )

        // SWAP
        val destination = getFixedDestination(destinationTokenMint)
        val destinationToken = destination.destinationToken
        val needsCreateDestinationTokenAccount = destination.needsCreateDestinationTokenAccount

        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val swappingFee = calculateSwappingFee(
            info = relayInfo,
            sourceToken = sourceToken,
            destinationToken = destinationToken,
            needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount
        )

        // TOP UP
        val topUpFeesAndPools: FeesAndPools?
        val topUpAmount: BigInteger?

        if (relayAccount.balance == null ||
            relayAccount.balance >= swappingFee.total ||
            tradableTopUpPoolsPair.isEmpty()
        ) {
            topUpFeesAndPools = null
            topUpAmount = null
        } else {
            // STEP 2.2: Else
            // Get best poolpairs for topping up
            topUpAmount = swappingFee.total - relayAccount.balance

            val topUpPools =
                orcaPoolInteractor.findBestPoolsPairForEstimatedAmount(topUpAmount, tradableTopUpPoolsPair)
                    ?: throw IllegalStateException("Swap pools not found")

            val topUpFee = feeRelayerTopUpInteractor.calculateTopUpFee(
                info = relayInfo,
                relayAccount = relayAccount
            )
            topUpFeesAndPools = FeesAndPools(topUpFee, topUpPools)
        }

        preparedParams = TopUpAndActionPreparedParams(
            topUpFeesAndPools = topUpFeesAndPools,
            actionFeesAndPools = FeesAndPools(swappingFee, swapPools),
            topUpAmount = topUpAmount
        )

        return preparedParams!!
    }

    /*
     * Get fixed destination
     * */
    private suspend fun getFixedDestination(destinationTokenMint: String): FixedDestination {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        // Redefine destination
        val userDestinationAccountOwnerAddress: PublicKey?
        val addressData: TransactionAddressData

        if (WRAPPED_SOL_MINT == destinationTokenMint) {
            // Swap to native SOL account
            userDestinationAccountOwnerAddress = owner
            addressData = TransactionAddressData(owner, true)
        } else {
            // Swap to other SPL
            userDestinationAccountOwnerAddress = null
            addressData = addressInteractor.findAssociatedAddress(
                destinationMint = destinationTokenMint,
                ownerAddress = owner
            )
        }

        val destinationToken = TokenInfo(
            address = addressData.associatedAddress.toBase58(),
            mint = destinationTokenMint
        )
        return FixedDestination(
            destinationToken = destinationToken,
            userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress,
            needsCreateDestinationTokenAccount = addressData.shouldCreateAssociatedInstruction
        )
    }
}