package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.utils.crypto.Base64Utils
import org.p2p.wallet.feerelayer.model.FeesAndPools
import org.p2p.wallet.feerelayer.model.FeesAndTopUpAmount
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.model.TopUpAndActionPreparedParams
import org.p2p.wallet.feerelayer.model.TopUpPreparedParams
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.transaction.TransactionManager
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigInteger

class FeeRelayerInteractor(
    private val rpcRepository: RpcRepository,
    private val feeRelayerRequestInteractor: FeeRelayerRequestInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val orcaSwapInteractor: OrcaSwapInteractor,
    private val transactionManager: TransactionManager,
    private val tokenKeyProvider: TokenKeyProvider
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

    private var preparedParams: TopUpAndActionPreparedParams? = null

    /*
    * STEP 0: Prepare all information needed for the transaction
    * Load all needed info for relay operations, need to be completed before any operation
    * */
    suspend fun load() = withContext(Dispatchers.IO) {
        feeRelayerAccountInteractor.getRelayInfo()
        feeRelayerAccountInteractor.getUserRelayAccount()
    }

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
            relayAccountStatus = relayAccount,
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

    /**
    Transfer an amount of spl token to destination address.
     * Parameters:
     * sourceToken: source that contains address of account and mint address.
     * destinationAddress: pass destination wallet address if spl token doesn't exist in this wallet. Otherwise pass wallet's token address.
     * tokenMint: the address of mint
     * inputAmount: the amount that will be transferred
     * payingFeeToken: the token that will be used to pay as fee
     * Returns:
     */
    suspend fun topUpAndSend(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        destinationAddress: String,
        tokenMint: String,
        inputAmount: BigInteger,
        payingFeeToken: TokenInfo
    ): List<String> {
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount(reuseCache = false)
        val tokenSupply = rpcRepository.getTokenSupply(tokenMint)

        val account = Account(tokenKeyProvider.secretKey)
        val info = feeRelayerAccountInteractor.getRelayInfo()

        val (transaction, amount) = feeRelayerRequestInteractor.makeTransferTransaction(
            programId = feeRelayerProgramId,
            owner = account,
            sourceToken = sourceToken,
            recipientPubkey = destinationAddress,
            tokenMintAddress = tokenMint,
            feePayerAddress = info.feePayerAddress.toBase58(),
            lamportsPerSignatures = info.lamportsPerSignature,
            minimumTokenAccountBalance = info.minimumTokenAccountBalance,
            inputAmount = inputAmount,
            decimals = tokenSupply.value.decimals
        )

        val params = prepareForTopUp(amount, payingFeeToken, relayAccount)

        transaction.sign(listOf(account))
        val blockhash = transaction.recentBlockHash
        // STEP 2: Check if relay account has already had enough balance to cover swapping fee
        // STEP 2.1: If relay account has enough balance to cover swapping fee
        return if (params.topUpFeesAndPools == null || params.topUpAmount == null) {
            feeRelayerRequestInteractor.relayTransaction(
                instructions = transaction.instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = blockhash
            )
        } else {
            // STEP 2.2.1: Top up
            feeRelayerRequestInteractor.topUp(
                owner = account,
                needsCreateUserRelayAddress = !relayAccount.isCreated,
                sourceToken = payingFeeToken,
                amount = params.topUpAmount,
                topUpPools = params.topUpFeesAndPools.poolsPair,
                topUpFee = params.topUpFeesAndPools.fee.total
            )

            feeRelayerRequestInteractor.relayTransaction(
                instructions = transaction.instructions,
                signatures = transaction.allSignatures,
                pubkeys = transaction.accountKeys,
                blockHash = blockhash
            )
        }
    }

    // Top up relay account (if needed) and swap
    suspend fun topUpAndSwap(
        feeRelayerProgramId: PublicKey,
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double
    ): List<String> {

        // get owner
        val owner = Account(tokenKeyProvider.secretKey)

        // get fresh data by ignoring cache
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount(false)

        val params = prepareForTopUpAndSwap(
            sourceToken = sourceToken,
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress,
            payingFeeToken = payingFeeToken,
            swapPools = swapPools,
            relayAccountStatus = relayAccount,
            reuseCache = false
        )

        val destination = getFixedDestination(destinationTokenMint, destinationAddress)

        val info = feeRelayerAccountInteractor.getRelayInfo()

        val destinationToken = destination.destinationToken
        val userDestinationAccountOwnerAddress = destination.userDestinationAccountOwnerAddress
        val needsCreateDestinationTokenAccount = destination.needsCreateDestinationTokenAccount

        val swapFeesAndPools = params.actionFeesAndPools
        val swappingFee = swapFeesAndPools.fee.total
        val swapPools = swapFeesAndPools.poolsPair

        // STEP 2: Check if relay account has already had enough balance to cover swapping fee
        // STEP 2.1: If relay account has enough balance to cover swapping fee
        return if (params.topUpFeesAndPools == null || params.topUpAmount == null) {
            swap(
                feeRelayerProgramId = feeRelayerProgramId,
                owner = owner,
                sourceToken = sourceToken,
                destinationToken = destinationToken,
                userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress?.toBase58(),
                pools = swapPools,
                inputAmount = inputAmount,
                slippage = slippage,
                feeAmount = swappingFee,
                minimumTokenAccountBalance = info.minimumTokenAccountBalance,
                needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount,
                feePayerAddress = info.feePayerAddress,
                lamportsPerSignature = info.lamportsPerSignature
            )
        } else {
            // STEP 2.2.1: Top up
            feeRelayerRequestInteractor.topUp(
                owner = owner,
                needsCreateUserRelayAddress = !relayAccount.isCreated,
                sourceToken = payingFeeToken,
                amount = params.topUpAmount,
                topUpPools = params.topUpFeesAndPools.poolsPair,
                topUpFee = params.topUpFeesAndPools.fee.total
            )

            // STEP 2.2.2: Swap
            swap(
                feeRelayerProgramId = feeRelayerProgramId,
                owner = owner,
                sourceToken = sourceToken,
                destinationToken = destinationToken,
                userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress?.toBase58(),

                pools = swapPools,
                inputAmount = inputAmount,
                slippage = slippage,

                feeAmount = swappingFee,
                minimumTokenAccountBalance = info.minimumTokenAccountBalance,
                needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount,
                feePayerAddress = info.feePayerAddress,
                lamportsPerSignature = info.lamportsPerSignature
            )
        }
    }

    suspend fun swap(
        feeRelayerProgramId: PublicKey,
        owner: Account,
        sourceToken: TokenInfo,
        destinationToken: TokenInfo,
        userDestinationAccountOwnerAddress: String?,
        pools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double,
        feeAmount: BigInteger,
        minimumTokenAccountBalance: BigInteger,
        needsCreateDestinationTokenAccount: Boolean,
        feePayerAddress: PublicKey,
        lamportsPerSignature: BigInteger
    ): List<String> {

        val recentBlockhash = rpcRepository.getRecentBlockhash()

        val preparedTransaction = feeRelayerRequestInteractor.prepareSwapTransaction(
            programId = feeRelayerProgramId,
            sourceToken = sourceToken,
            destinationToken = destinationToken,
            userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress,
            pools = pools,
            inputAmount = inputAmount,
            slippage = slippage,
            feeAmount = feeAmount,
            blockhash = recentBlockhash.recentBlockhash,
            minimumTokenAccountBalance = minimumTokenAccountBalance,
            needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount,
            feePayerAddress = feePayerAddress,
            lamportsPerSignature = lamportsPerSignature
        )

        val transaction = preparedTransaction.transaction
        transactionManager.emitTransactionId(transaction.signature.signature)

        val tm = transaction.serialize()
        val st = Base64Utils.encode(tm)
        Timber.d("### message $st")

        return feeRelayerRequestInteractor.relayTransaction(
            instructions = transaction.instructions,
            signatures = transaction.allSignatures,
            pubkeys = transaction.accountKeys,
            blockHash = recentBlockhash.recentBlockhash
        )
    }

    private suspend fun prepareForTopUpAndSwap(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair,
        relayAccountStatus: RelayAccount,
        reuseCache: Boolean
    ): TopUpAndActionPreparedParams {

        if (preparedParams == null || !reuseCache) {
            val tradableTopUpPoolsPair = orcaSwapInteractor.getTradablePoolsPairs(
                fromMint = payingFeeToken.mint,
                toMint = WRAPPED_SOL_MINT
            )

            // SWAP
            val destination = getFixedDestination(
                destinationTokenMint = destinationTokenMint,
                destinationAddress = destinationAddress
            )
            val destinationToken = destination.destinationToken
            val needsCreateDestinationTokenAccount = destination.needsCreateDestinationTokenAccount

            val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
            val swappingFee = feeRelayerRequestInteractor.calculateSwappingFee(
                info = relayInfo,
                sourceToken = sourceToken,
                destinationToken = destinationToken,
                needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount
            )

            // TOP UP
            val topUpFeesAndPools: FeesAndPools?
            val topUpAmount: BigInteger?

            if (relayAccountStatus.balance == null ||
                relayAccountStatus.balance >= swappingFee.total ||
                tradableTopUpPoolsPair.isEmpty()
            ) {
                topUpFeesAndPools = null
                topUpAmount = null
            } else {
                // STEP 2.2: Else
                // Get best poolpairs for topping up
                topUpAmount = swappingFee.total - relayAccountStatus.balance

                val topUpPools =
                    orcaSwapInteractor.findBestPoolsPairForEstimatedAmount(topUpAmount, tradableTopUpPoolsPair)
                        ?: throw IllegalStateException("Swap pools not found")

                val topUpFee = feeRelayerInstructionsInteractor.calculateTopUpFee(
                    info = relayInfo,
                    topUpPools = topUpPools,
                    relayAccountStatus = relayAccountStatus
                )
                topUpFeesAndPools = FeesAndPools(topUpFee, topUpPools)
            }

            preparedParams = TopUpAndActionPreparedParams(
                topUpFeesAndPools = topUpFeesAndPools,
                actionFeesAndPools = FeesAndPools(swappingFee, swapPools),
                topUpAmount = topUpAmount
            )
        }

        return preparedParams!!
    }

    /*
     * Get fixed destination
     * */
    private fun getFixedDestination(
        destinationTokenMint: String,
        destinationAddress: String?
    ): FixedDestination {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        val needsCreateDestinationTokenAccount: Boolean
        val userDestinationAddress: String
        val userDestinationAccountOwnerAddress: PublicKey?

        if (owner.toBase58() == destinationAddress) {
            // Swap to native SOL account
            userDestinationAccountOwnerAddress = owner
            needsCreateDestinationTokenAccount = true
            userDestinationAddress = owner.toBase58()
        } else {
            // Swap to other SPL
            userDestinationAccountOwnerAddress = null
            if (destinationAddress != null) {
                // SPL token has ALREADY been created
                userDestinationAddress = destinationAddress
                needsCreateDestinationTokenAccount = false
            } else {
                // SPL token has NOT been created
                userDestinationAddress = owner.toBase58()
                needsCreateDestinationTokenAccount = true
            }
        }

        val destinationToken = TokenInfo(address = userDestinationAddress, mint = destinationTokenMint)
        return FixedDestination(
            destinationToken,
            userDestinationAccountOwnerAddress,
            needsCreateDestinationTokenAccount
        )
    }

    private suspend fun prepareForTopUp(
        feeAmount: FeeAmount,
        payingFeeToken: TokenInfo,
        relayAccountStatus: RelayAccount
    ): TopUpPreparedParams {

        val tradableTopUpPoolsPair = orcaSwapInteractor.getTradablePoolsPairs(payingFeeToken.mint, WRAPPED_SOL_MINT)
        // TOP UP
        val topUpFeesAndPools: FeesAndPools?
        val topUpAmount: BigInteger?
        if (relayAccountStatus.balance != null && relayAccountStatus.balance >= feeAmount.total) {
            // if relay account balance is enought, skipping top up
            topUpFeesAndPools = null
            topUpAmount = null
        } else {
            // STEP 2.2: Else

            // otherwise, deposit to relay account balance
            // Get best poolpairs for topping up
            topUpAmount = feeAmount.total - (relayAccountStatus.balance ?: BigInteger.ZERO)

            val topUpPools = orcaSwapInteractor.findBestPoolsPairForEstimatedAmount(topUpAmount, tradableTopUpPoolsPair)
            if (topUpPools.isNullOrEmpty()) {
                throw IllegalStateException("Swap pools not found")
            }

            val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
            val topUpFee = feeRelayerInstructionsInteractor.calculateTopUpFee(relayInfo, topUpPools, relayAccountStatus)
            topUpFeesAndPools = FeesAndPools(topUpFee, topUpPools)

            val routeValues = topUpPools.joinToString { "${it.tokenAName} -> ${it.tokenBName} (${it.deprecated})" }
            Timber.tag("POOLPAIR_SEND").d(routeValues)
        }

        return TopUpPreparedParams(topUpFeesAndPools, topUpAmount)
    }

    class FixedDestination(
        val destinationToken: TokenInfo,
        val userDestinationAccountOwnerAddress: PublicKey?,
        val needsCreateDestinationTokenAccount: Boolean
    )
}