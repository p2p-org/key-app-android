package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.programs.SystemProgram
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.model.PreparedParams
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerInstructionsInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val environmentManager: EnvironmentManager
) {

    /*
    * Prepare swap data from swap pools
    * */
    fun prepareSwapData(
        pools: OrcaPoolsPair,
        inputAmount: BigInteger?,
        minAmountOut: BigInteger?,
        slippage: Double,
        transitTokenMintPubkey: PublicKey? = null,
        newTransferAuthority: Boolean = false
    ): Pair<SwapData, Account?> {
        val owner = tokenKeyProvider.publicKey
        // preconditions
        if (pools.size == 0 || pools.size > 2) {
            throw IllegalStateException("Swap pools not found")
        }

        if (inputAmount == null && minAmountOut == null) {
            throw IllegalStateException("Invalid amount")
        }

        // create transferAuthority
        val transferAuthority = Account()

        // form topUp params
        val transferAuthorityPubkey = if (newTransferAuthority) {
            transferAuthority.publicKey
        } else {
            owner.toPublicKey()
        }
        if (pools.size == 1) {
            val pool = pools[0]

            val amountIn = inputAmount ?: pool.getInputAmount(minAmountOut!!, slippage)
            val minAmountOut = minAmountOut ?: pool.getMinimumAmountOut(inputAmount!!, slippage)

            if (amountIn == null || minAmountOut == null) {
                throw IllegalStateException("Invalid amount")
            }

            val directSwapData = pool.getSwapData(
                transferAuthorityPubkey = transferAuthorityPubkey,
                amountIn = amountIn,
                minAmountOut = minAmountOut
            )

            val transferAuthorityAddress = if (newTransferAuthority) transferAuthority else null
            return directSwapData to transferAuthorityAddress
        } else {
            val firstPool = pools[0]
            val secondPool = pools[1]

            if (transitTokenMintPubkey == null) {
                throw IllegalStateException("Transit token mint not found")
            }

            // if input amount is provided
            var firstPoolAmountIn = inputAmount
            var secondPoolAmountIn: BigInteger? = null
            var secondPoolAmountOut = minAmountOut

            if (inputAmount != null) {
                secondPoolAmountIn = firstPool.getMinimumAmountOut(inputAmount, slippage) ?: BigInteger.ZERO
                secondPoolAmountOut = secondPool.getMinimumAmountOut(secondPoolAmountIn!!, slippage)
            } else if (minAmountOut != null) {
                secondPoolAmountIn = secondPool.getInputAmount(minAmountOut, slippage) ?: BigInteger.ZERO
                firstPoolAmountIn = firstPool.getInputAmount(secondPoolAmountIn!!, slippage)
            }

            if (firstPoolAmountIn == null || secondPoolAmountIn == null || secondPoolAmountOut == null) {
                throw IllegalStateException("Invalid amount")
            }

            val transitiveSwapData = SwapData.SplTransitive(
                from = firstPool.getSwapData(
                    transferAuthorityPubkey = transferAuthorityPubkey,
                    amountIn = firstPoolAmountIn,
                    minAmountOut = secondPoolAmountIn
                ),
                to = secondPool.getSwapData(
                    transferAuthorityPubkey = transferAuthorityPubkey,
                    amountIn = secondPoolAmountIn,
                    minAmountOut = secondPoolAmountOut
                ),
                transitTokenMintPubkey = transitTokenMintPubkey.toBase58()
            )

            return transitiveSwapData to if (newTransferAuthority) transferAuthority else null
        }
    }

    /*
    * Calculate needed fee for topup transaction by forming fake transaction
    * */
    fun calculateTopUpFee(
        info: RelayInfo,
        topUpPools: OrcaPoolsPair,
        relayAccountStatus: RelayAccount
    ): FeeAmount {
        val fee = prepareForTopUp(
            sourceToken = TokenInfo(
                address = "C5B13tQA4pq1zEVSVkWbWni51xdWB16C2QsC72URq9AJ", // fake
                mint = "2Kc38rfQ49DFaKHQaWbijkE7fcymUMLY5guUiUsDmFfn" // fake
            ),
            userAuthorityAddress = "5bYReP8iw5UuLVS5wmnXfEfrYCKdiQ1FFAZQao8JqY7V".toPublicKey(), // fake
            userRelayAddress = "EfS3E3jBF6iio6zQDVWswj3mtoHMGEq57iqpPRgTBVUt".toPublicKey(), // fake
            topUpPools = topUpPools,
            amount = BigInteger.valueOf(10000L), // fake
            feeAmount = BigInteger.ZERO, // fake
            blockhash = "FR1GgH83nmcEdoNXyztnpUL2G13KkUv6iwJPwVfnqEgW", // fake
            minimumRelayAccountBalance = info.minimumRelayAccountBalance,
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            needsCreateUserRelayAccount = !relayAccountStatus.isCreated,
            feePayerAddress = "FG4Y3yX4AAchp1HvNZ7LfzFTewF2f6nDoMDCohTFrdpT", // fake
            lamportsPerSignature = info.lamportsPerSignature
        ).feeAmount
        return fee
    }

    /*
    * Prepare transaction and expected fee for a given relay transaction
    * */
    fun prepareForTopUp(
        sourceToken: TokenInfo,
        userAuthorityAddress: PublicKey,
        userRelayAddress: PublicKey,
        topUpPools: OrcaPoolsPair,
        amount: BigInteger,
        feeAmount: BigInteger,
        blockhash: String,
        minimumRelayAccountBalance: BigInteger,
        minimumTokenAccountBalance: BigInteger,
        needsCreateUserRelayAccount: Boolean,
        feePayerAddress: String,
        lamportsPerSignature: BigInteger
    ): PreparedParams {
        val programId = FeeRelayerProgram.getProgramId(!environmentManager.isDevnet())
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
        var expectedFee = FeeAmount(BigInteger.ZERO, BigInteger.ZERO)
        val instructions = mutableListOf<TransactionInstruction>()

        // create user relay account
        if (needsCreateUserRelayAccount) {
            val transferInstruction = SystemProgram.transfer(
                fromPublicKey = feePayerAddress.toPublicKey(),
                toPublicKey = userRelayAddress,
                lamports = minimumTokenAccountBalance
            )
            instructions += transferInstruction

            expectedFee.accountBalances += minimumRelayAccountBalance
        }

        // top up swap
        val transitTokenMintPubkey = getTransitTokenMintPubkey(topUpPools)
        val (topUpSwap, transferAuthority) = prepareSwapData(
            pools = topUpPools,
            inputAmount = null,
            minAmountOut = amount,
            slippage = 0.01,
            transitTokenMintPubkey = transitTokenMintPubkey
        )

        when (topUpSwap) {
            is SwapData.Direct -> {
                expectedFee.accountBalances += minimumTokenAccountBalance

                // approve
                val approveInstruction = TokenProgram.approveInstruction(
                    TokenProgram.PROGRAM_ID,
                    userSourceTokenAccountAddress,
                    PublicKey(topUpSwap.transferAuthorityPubkey),
                    userAuthorityAddress,
                    topUpSwap.amountIn
                )
                instructions += approveInstruction

                // top up
                val userTemporarilyWSOLAddress = feeRelayerAccountInteractor.getUserTemporaryWsolAccount(
                    userAuthorityAddress
                )
                // overriding
                val userRelayAddress = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress)

                val topUpSwapInstruction = FeeRelayerProgram.topUpSwapDirectInstruction(
                    programId = programId,
                    feePayer = feePayerAddress.toPublicKey(),
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = userRelayAddress,
                    userTransferAuthority = PublicKey(topUpSwap.transferAuthorityPubkey),
                    userSourceTokenAccount = userSourceTokenAccountAddress,
                    userTemporaryWsolAccount = userTemporarilyWSOLAddress,
                    swapProgramId = PublicKey(topUpSwap.programId),
                    swapAccount = PublicKey(topUpSwap.accountPubkey),
                    swapAuthority = PublicKey(topUpSwap.authorityPubkey),
                    swapSource = PublicKey(topUpSwap.sourcePubkey),
                    swapDestination = PublicKey(topUpSwap.destinationPubkey),
                    poolTokenMint = PublicKey(topUpSwap.poolTokenMintPubkey),
                    poolFeeAccount = PublicKey(topUpSwap.poolFeeAccountPubkey),
                    amountIn = topUpSwap.amountIn,
                    minimumAmountOut = topUpSwap.minimumAmountOut,
                )
                instructions += topUpSwapInstruction

                // transfer
                val transferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
                    programId = programId,
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress),
                    recipient = feePayerAddress.toPublicKey(),
                    amount = feeAmount
                )
                instructions += transferSolInstruction
            }
            is SwapData.SplTransitive -> {
                // approve
                val approveInstruction = TokenProgram.approveInstruction(
                    TokenProgram.PROGRAM_ID,
                    userSourceTokenAccountAddress,
                    topUpSwap.from.transferAuthorityPubkey.toPublicKey(),
                    userAuthorityAddress,
                    topUpSwap.from.amountIn
                )
                instructions += approveInstruction

                // create transit token account
                val transitTokenMint = topUpSwap.transitTokenMintPubkey.toPublicKey()
                val transitTokenAccountAddress = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                    owner = userAuthorityAddress,
                    mint = transitTokenMint
                )

                val createTransitInstruction = FeeRelayerProgram.createTransitTokenAccountInstruction(
                    programId = programId,
                    feePayer = feePayerAddress.toPublicKey(),
                    userAuthority = userAuthorityAddress,
                    transitTokenAccount = transitTokenAccountAddress,
                    transitTokenMint = transitTokenMint,
                )
                instructions += createTransitInstruction

                // Destination WSOL account funding
                expectedFee.accountBalances += minimumTokenAccountBalance

                // top up
                val userTemporarilyWSOLAddress = feeRelayerAccountInteractor.getUserTemporaryWsolAccount(
                    userAuthorityAddress
                )
                // overriding
                val userRelayAddress = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress)

                val topUpSwapInstruction = FeeRelayerProgram.createRelayTopUpSwapTransitiveInstruction(
                    programId = programId,
                    feePayer = feePayerAddress.toPublicKey(),
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = userRelayAddress,
                    userTransferAuthority = PublicKey(topUpSwap.from.transferAuthorityPubkey),
                    userSourceTokenAccount = userSourceTokenAccountAddress,
                    userDestinationTokenAccount = userTemporarilyWSOLAddress,
                    userTransitTokenAccount = transitTokenAccountAddress,
                    swapFromProgramId = PublicKey(topUpSwap.from.programId),
                    swapFromAccount = PublicKey(topUpSwap.from.accountPubkey),
                    swapFromAuthority = PublicKey(topUpSwap.from.authorityPubkey),
                    swapFromSource = PublicKey(topUpSwap.from.sourcePubkey),
                    swapFromDestination = PublicKey(topUpSwap.from.destinationPubkey),
                    swapFromPoolTokenMint = PublicKey(topUpSwap.from.poolTokenMintPubkey),
                    swapFromPoolFeeAccount = PublicKey(topUpSwap.from.poolFeeAccountPubkey),
                    swapToProgramId = PublicKey(topUpSwap.to.programId),
                    swapToAccount = PublicKey(topUpSwap.to.accountPubkey),
                    swapToAuthority = PublicKey(topUpSwap.to.authorityPubkey),
                    swapToSource = PublicKey(topUpSwap.to.sourcePubkey),
                    swapToDestination = PublicKey(topUpSwap.to.destinationPubkey),
                    swapToPoolTokenMint = PublicKey(topUpSwap.to.poolTokenMintPubkey),
                    swapToPoolFeeAccount = PublicKey(topUpSwap.to.poolFeeAccountPubkey),
                    amountIn = topUpSwap.from.amountIn,
                    transitMinimumAmount = topUpSwap.from.minimumAmountOut,
                    minimumAmountOut = topUpSwap.to.minimumAmountOut,
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

                // transfer
                val transferSolInstruction = FeeRelayerProgram.createRelayTransferSolInstruction(
                    programId = programId,
                    userAuthority = userAuthorityAddress,
                    userRelayAccount = feeRelayerAccountInteractor.getUserRelayAddress(userAuthorityAddress),
                    recipient = feePayerAddress.toPublicKey(),
                    amount = feeAmount
                )
                instructions += transferSolInstruction
            }
        }

        val transaction = Transaction()
        transaction.addInstructions(instructions)
        transaction.setFeePayer(feePayerAddress.toPublicKey())
        transaction.recentBlockHash = blockhash

        val transactionFee = transaction.calculateTransactionFee(lamportsPerSignature)
        expectedFee = expectedFee.copy(transaction = transactionFee)

        return PreparedParams(
            swapData = topUpSwap,
            transaction = transaction,
            feeAmount = expectedFee,
            transferAuthorityAccount = transferAuthority
        )
    }

    fun getTransitTokenMintPubkey(pools: OrcaPoolsPair): PublicKey? {
        var transitTokenMintPubkey: PublicKey? = null
        if (pools.size == 2) {
            val interTokenName = pools[0].tokenBName
            val pubkey = userLocalRepository.findTokenDataBySymbol(interTokenName)?.mintAddress
            transitTokenMintPubkey = pubkey?.let { PublicKey(pubkey) }
        }
        return transitTokenMintPubkey
    }

    private fun OrcaPool.getSwapData(
        transferAuthorityPubkey: PublicKey,
        amountIn: BigInteger,
        minAmountOut: BigInteger
    ): SwapData.Direct =
        SwapData.Direct(
            programId = swapProgramId.toBase58(),
            accountPubkey = account.toBase58(),
            authorityPubkey = authority.toBase58(),
            transferAuthorityPubkey = transferAuthorityPubkey.toBase58(),
            sourcePubkey = tokenAccountA.toBase58(),
            destinationPubkey = tokenAccountB.toBase58(),
            poolTokenMintPubkey = poolTokenMint.toBase58(),
            poolFeeAccountPubkey = feeAccount.toBase58(),
            amountIn = amountIn,
            minimumAmountOut = minAmountOut
        )
}