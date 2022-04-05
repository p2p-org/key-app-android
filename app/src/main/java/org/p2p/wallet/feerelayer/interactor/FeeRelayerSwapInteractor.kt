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
import org.p2p.wallet.feerelayer.model.FixedDestination
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.model.TopUpAndActionPreparedParams
import org.p2p.wallet.feerelayer.model.TopUpPreparedParams
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.repository.blockhash.RpcBlockhashRepository
import org.p2p.wallet.swap.interactor.orca.OrcaPoolInteractor
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.TransactionAddressData
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerSwapInteractor(
    private val rpcRepository: RpcBlockhashRepository,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val addressInteractor: TransactionAddressInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val tokenKeyProvider: TokenKeyProvider
) {

    private var preparedParams: TopUpAndActionPreparedParams? = null

    // Prepare swap transaction for relay
    suspend fun prepareSwapTransaction(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double
    ): Pair<List<PreparedTransaction>, BigInteger> {
        val info = feeRelayerAccountInteractor.getRelayInfo()

        val preparedParams = prepareForTopUpAndSwap(
            sourceToken = sourceToken,
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress,
            payingFeeToken = payingFeeToken,
            swapPools = swapPools,
            reuseCache = false
        )
        val destination = getFixedDestination(
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress
        )

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

    suspend fun calculateSwappingNetworkFees(
        swapPools: OrcaPoolsPair?,
        sourceTokenMint: String,
        destinationTokenMint: String,
        destinationAddress: String?
    ): FeeAmount {
        val destination = getFixedDestination(
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress
        )
        val info = feeRelayerAccountInteractor.getRelayInfo()
        val lamportsPerSignature = info.lamportsPerSignature
        val minimumTokenAccountBalance = info.minimumTokenAccountBalance

        val needsCreateDestinationTokenAccount = destination.needsCreateDestinationTokenAccount

        val expectedFee = FeeAmount()

        // fee for payer's signature
        expectedFee.transaction += lamportsPerSignature

        // fee for owner's signature
        expectedFee.transaction += lamportsPerSignature

        // when source token is native SOL
        if (sourceTokenMint == WRAPPED_SOL_MINT) {
            // WSOL's signature
            expectedFee.transaction += lamportsPerSignature
        }

        // when needed to create destination
        if (needsCreateDestinationTokenAccount && destinationTokenMint != WRAPPED_SOL_MINT) {
            expectedFee.accountBalances += minimumTokenAccountBalance
        }

        // when destination is native SOL
        if (destinationTokenMint == WRAPPED_SOL_MINT) {
            expectedFee.transaction += lamportsPerSignature
        }

        // in transitive swap, there will be situation when swapping from SOL -> SPL that needs spliting transaction to 2 transactions
        if (swapPools?.size == 2 && sourceTokenMint == WRAPPED_SOL_MINT && destinationAddress == null) {
            expectedFee.transaction += lamportsPerSignature * BigInteger.valueOf(2L)
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
    ): Pair<List<PreparedTransaction>, BigInteger> {
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
        var additionalPaybackFee: BigInteger = BigInteger.ZERO

        // check source
        var sourceWSOLNewAccount: Account? = null
        if (sourceToken.mint == WRAPPED_SOL_MINT) {
            sourceWSOLNewAccount = Account()
            val createAccountInstruction = SystemProgram.createAccount(
                userAuthorityAddress,
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
            // TODO: refactor this class. This hack is to prevent additional fee payment
            if (!sourceToken.isSOL || needsCreateDestinationTokenAccount) {
                additionalPaybackFee += minimumTokenAccountBalance
            }
            userSourceTokenAccountAddress = sourceWSOLNewAccount.publicKey
        }

        var accountCreationTransaction: PreparedTransaction? = null

        // check destination
        var destinationNewAccount: Account? = null
        var userDestinationTokenAccountAddress = destinationToken.address.toPublicKey()
        if (needsCreateDestinationTokenAccount) {
            if (destinationTokenMintAddress.toBase58() == WRAPPED_SOL_MINT) {
                // For native solana, create and initialize WSOL
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
                userDestinationTokenAccountAddress = destinationNewAccount.publicKey
                accountCreationFee += minimumTokenAccountBalance
            } else {
                // For other token, create associated token address
                val associatedAddress = TokenTransaction.getAssociatedTokenAddress(
                    mint = destinationTokenMintAddress,
                    owner = userAuthorityAddress
                )

                val createAssociatedTokenAccountInstruction = TokenProgram.createAssociatedTokenAccountInstruction(
                    TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                    TokenProgram.PROGRAM_ID,
                    destinationTokenMintAddress,
                    associatedAddress,
                    userAuthorityAddress,
                    feePayerAddress
                )

                /*
                * Case when user swaps SOL to non-created SPL
                * Account creation may cause [Transaction too large] exception
                * Therefore, we are splitting creation and transfer transactions to separate transactions
                * First is creation
                * Second is transfer
                * */
                if (sourceWSOLNewAccount != null) {
                    accountCreationTransaction = prepareTransaction(
                        instructions = listOf(createAssociatedTokenAccountInstruction),
                        signers = listOf(owner),
                        blockhash = blockhash,
                        feePayerAddress = feePayerAddress,
                        accountCreationFee = accountCreationFee,
                        lamportsPerSignature = lamportsPerSignature
                    )
                } else {
                    instructions += createAssociatedTokenAccountInstruction
                    accountCreationFee += minimumTokenAccountBalance
                }

                userDestinationTokenAccountAddress = associatedAddress
            }
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

        when (swapData) {
            is SwapData.Direct -> {
                // swap
                val pool = pools.first()
                val swapInstruction = TokenSwapProgram.swapInstruction(
                    pool.account,
                    pool.authority,
                    userAuthorityAddress,
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

        // resign transaction
        val signers = mutableListOf(owner)
        if (sourceWSOLNewAccount != null) {
            signers += sourceWSOLNewAccount
        }

        if (destinationNewAccount != null) {
            signers += destinationNewAccount
        }

        val transactions = mutableListOf<PreparedTransaction>()
        if (accountCreationTransaction != null) {
            transactions += accountCreationTransaction
        }

        transactions += prepareTransaction(
            instructions = instructions,
            signers = signers,
            blockhash = blockhash,
            feePayerAddress = feePayerAddress,
            accountCreationFee = accountCreationFee,
            lamportsPerSignature = lamportsPerSignature
        )

        return transactions to additionalPaybackFee
    }

    private suspend fun prepareForTopUpAndSwap(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair,
        reuseCache: Boolean
    ): TopUpAndActionPreparedParams {
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()
        val freeTransactionFeeLimit = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()

        if (reuseCache && preparedParams != null) {
            return preparedParams!!
        }

        val swappingFee = calculateSwappingNetworkFees(
            swapPools = swapPools,
            sourceTokenMint = sourceToken.mint,
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress
        )

        // TOP UP
        val topUpPreparedParam: TopUpPreparedParams?

        if (payingFeeToken.isSOL || (relayAccount.balance != null && relayAccount.balance >= swappingFee.total)) {
            topUpPreparedParam = null
        } else {
            // Get real amounts needed for topping up
            val tradableTopUpPoolsPair = orcaPoolInteractor.getTradablePoolsPairs(
                fromMint = payingFeeToken.mint,
                toMint = WRAPPED_SOL_MINT
            )

            val topUpAmount = feeRelayerTopUpInteractor.calculateNeededTopUpAmount(
                swappingFee,
                payingFeeToken.mint
            ).total

            val expectedFee = feeRelayerTopUpInteractor.calculateExpectedFeeForTopUp(
                relayAccount = relayAccount,
                freeTransactionFeeLimit = freeTransactionFeeLimit
            )

            // Get pools
            val topUpPools = orcaPoolInteractor.findBestPoolsPairForEstimatedAmount(
                estimatedAmount = topUpAmount,
                poolsPairs = tradableTopUpPoolsPair
            )

            if (topUpPools.isNullOrEmpty()) {
                throw IllegalStateException("Swap pools not found")
            }

            topUpPreparedParam = TopUpPreparedParams(
                amount = topUpAmount,
                expectedFee = expectedFee,
                poolsPair = topUpPools
            )
        }

        preparedParams = TopUpAndActionPreparedParams(
            topUpPreparedParam = topUpPreparedParam,
            actionFeesAndPools = FeesAndPools(swappingFee, swapPools)
        )

        return preparedParams!!
    }

    /*
     * Get fixed destination
     * */
    private suspend fun getFixedDestination(
        destinationTokenMint: String,
        destinationAddress: String?
    ): FixedDestination {
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

            addressData = if (destinationAddress.isNullOrEmpty()) {
                addressInteractor.findSplTokenAddressData(
                    destinationAddress = owner,
                    mintAddress = destinationTokenMint
                )
            } else {
                val destinationAddressPublicKey = destinationAddress.toPublicKey()
                TransactionAddressData(destinationAddressPublicKey, false)
            }
        }

        val destinationToken = TokenInfo(
            address = addressData.destinationAddress.toBase58(),
            mint = destinationTokenMint
        )
        return FixedDestination(
            destinationToken = destinationToken,
            userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress,
            needsCreateDestinationTokenAccount = addressData.shouldCreateAccount
        )
    }

    private fun prepareTransaction(
        instructions: List<TransactionInstruction>,
        signers: List<Account>,
        blockhash: String,
        feePayerAddress: PublicKey,
        accountCreationFee: BigInteger,
        lamportsPerSignature: BigInteger
    ): PreparedTransaction {
        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.recentBlockHash = blockhash
        transaction.feePayer = feePayerAddress

        // calculate fee first
        val expectedFee = FeeAmount(
            transaction = transaction.calculateTransactionFee(lamportsPerSignature),
            accountBalances = accountCreationFee
        )

        transaction.sign(signers)

        return PreparedTransaction(transaction, signers, expectedFee)
    }
}
