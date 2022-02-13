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
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.RelayInfo
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerInstructionsInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor
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
        newTransferAuthority: Boolean = true,
        userAuthorityAddress: PublicKey
    ): Pair<SwapData, Account?> {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
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
        if (pools.size == 1) {
            val pool = pools[0]

            val amountIn = inputAmount ?: pool.getInputAmount(minAmountOut!!, slippage)
            val minAmountOut = minAmountOut ?: pool.getMinimumAmountOut(inputAmount!!, slippage)

            if (amountIn == null || minAmountOut == null) {
                throw IllegalStateException("Invalid amount")
            }

            val directSwapData = pool.getSwapData(
                transferAuthorityPubkey = if (newTransferAuthority) transferAuthority.publicKey else owner,
                amountIn = amountIn,
                minAmountOut = minAmountOut
            )

            return directSwapData to if (newTransferAuthority) transferAuthority else null
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
                    transferAuthorityPubkey = if (newTransferAuthority) transferAuthority.publicKey else owner,
                    amountIn = firstPoolAmountIn,
                    minAmountOut = secondPoolAmountIn
                ),
                to = secondPool.getSwapData(
                    transferAuthorityPubkey = if (newTransferAuthority) transferAuthority.publicKey else owner,
                    amountIn = secondPoolAmountIn,
                    minAmountOut = secondPoolAmountOut
                ),
                transitTokenMintPubkey = transitTokenMintPubkey.toBase58(),
                transitTokenAccountAddress = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                    owner = userAuthorityAddress,
                    mint = transitTokenMintPubkey
                )
            )

            return transitiveSwapData to if (newTransferAuthority) transferAuthority else null
        }
    }

    /*
    * Calculate needed fee for topup transaction by forming fake transaction
    * */
    fun calculateTopUpFee(
        feeRelayerProgramId: PublicKey,
        info: RelayInfo,
        topUpPools: OrcaPoolsPair,
        relayAccountStatus: RelayAccount
    ): FeeAmount {
        val fee = prepareForTopUp(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = TokenInfo(
                address = "C5B13tQA4pq1zEVSVkWbWni51xdWB16C2QsC72URq9AJ", // fake
                mint = "2Kc38rfQ49DFaKHQaWbijkE7fcymUMLY5guUiUsDmFfn" // fake
            ),
            userAuthorityAddress = "5bYReP8iw5UuLVS5wmnXfEfrYCKdiQ1FFAZQao8JqY7V".toPublicKey(), // fake
            userRelayAddress = "EfS3E3jBF6iio6zQDVWswj3mtoHMGEq57iqpPRgTBVUt".toPublicKey(), // fake
            topUpPools = topUpPools,
            amount = BigInteger.valueOf(10000L), // fake
            feeAmount = FeeAmount(), // fake
            blockhash = "FR1GgH83nmcEdoNXyztnpUL2G13KkUv6iwJPwVfnqEgW", // fake
            minimumRelayAccountBalance = info.minimumRelayAccountBalance,
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            needsCreateUserRelayAccount = !relayAccountStatus.isCreated,
            feePayerAddress = "FG4Y3yX4AAchp1HvNZ7LfzFTewF2f6nDoMDCohTFrdpT", // fake
            lamportsPerSignature = info.lamportsPerSignature
        ).second.expectedFee
        return fee
    }

    /*
    * Prepare transaction and expected fee for a given relay transaction
    * */
    fun prepareForTopUp(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        userAuthorityAddress: PublicKey,
        userRelayAddress: PublicKey,
        topUpPools: OrcaPoolsPair,
        amount: BigInteger,
        feeAmount: FeeAmount,
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
        val transitTokenMintPubkey = getTransitTokenMintPubkey(topUpPools)
        val (topUpSwap, transferAuthorityAccount) = prepareSwapData(
            pools = topUpPools,
            inputAmount = null,
            minAmountOut = amount,
            slippage = 0.01,
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
            amount = feeAmount.accountBalances
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