package org.p2p.wallet.feerelayer.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.Transaction
import org.p2p.solanaj.utils.crypto.Base58Utils
import org.p2p.wallet.R
import org.p2p.wallet.feerelayer.model.FeeAmount
import org.p2p.wallet.feerelayer.model.FeesAndPools
import org.p2p.wallet.feerelayer.model.FeesAndTopUpAmount
import org.p2p.wallet.feerelayer.model.RelayAccount
import org.p2p.wallet.feerelayer.model.SwapTransactionSignatures
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.model.TopUpAndActionPreparedParams
import org.p2p.wallet.feerelayer.repository.FeeRelayerRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.model.TransactionResult
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.interactor.orca.OrcaSwapInteractor
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPool.Companion.getInputAmount
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerInteractor(
    private val feeRelayerRepository: FeeRelayerRepository,
    private val rpcRepository: RpcRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerInstructionsInteractor: FeeRelayerInstructionsInteractor,
    private val orcaSwapInteractor: OrcaSwapInteractor,
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
    * Load all needed info for relay operations, need to be completed before any operation
    * */
    suspend fun load() = withContext(Dispatchers.IO) {
        feeRelayerAccountInteractor.getRelayInfo()
        feeRelayerAccountInteractor.getUserRelayAccount()
    }

    /*
    *  Calculate fee and need amount for topup and swap
    * */
    suspend fun calculateFeeAndNeededTopUpAmountForSwapping(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: Token.Active,
        swapPools: OrcaPoolsPair
    ): FeesAndTopUpAmount {
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount()

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
            topUpAmountInPayingToen = topUpAmountInPayingToken
        )
    }

    suspend fun topUpAndSwap(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: TokenInfo,
        swapPools: OrcaPoolsPair,
        inputAmount: BigInteger,
        slippage: Double
    ): TransactionResult {
        // get owner
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        // TODO: Remove later, currently does not support swap from native SOL
        if (sourceToken.address == owner.toBase58()) {
            throw IllegalStateException("Unsupported swap")
        }

        // get fresh data by ignoring cache
        val relayAccount = feeRelayerAccountInteractor.getUserRelayAccount(false)
        return TransactionResult.Error(R.string.common_not_implemented_yet)
    }

    suspend fun prepareForTopUpAndSwap(
        sourceToken: TokenInfo,
        destinationTokenMint: String,
        destinationAddress: String?,
        payingFeeToken: Token.Active,
        swapPools: OrcaPoolsPair,
        relayAccountStatus: RelayAccount,
        reuseCache: Boolean
    ): TopUpAndActionPreparedParams {

        if (preparedParams == null || !reuseCache) {
            val tradableTopUpPoolsPair = orcaSwapInteractor.getTradablePoolsPairs(
                fromMint = payingFeeToken.mintAddress,
                toMint = Token.WRAPPED_SOL_MINT
            )

            // SWAP
            val destination = getFixedDestination(
                destinationTokenMint = destinationTokenMint,
                destinationAddress = destinationAddress
            )
            val destinationToken = destination.destinationToken
            val userDestinationAccountOwnerAddress = destination.userDestinationAccountOwnerAddress
            val needsCreateDestinationTokenAccount = destination.needsCreateDestinationTokenAccount

            val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
            val swappingFee = feeRelayerInstructionsInteractor.calculateSwappingFee(
                info = relayInfo,
                sourceToken = sourceToken,
                destinationToken = destinationToken,
                userDestinationAccountOwnerAddress = userDestinationAccountOwnerAddress?.toBase58(),
                pools = swapPools,
                needsCreateDestinationTokenAccount = needsCreateDestinationTokenAccount
            )

            // TOP UP
            val topUpFeesAndPools: FeesAndPools?
            val topUpAmount: BigInteger?

            if (relayAccountStatus.balance == null || relayAccountStatus.balance >= swappingFee.total) {
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
                    relayInfo,
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

    suspend fun topUpWithSwap(
        owner: Account,
        needsCreateUserRelayAddress: Boolean,
        sourceToken: Token.Active,
        amount: BigInteger,
        topUpPools: OrcaPoolsPair,
        topUpFee: BigInteger
    ): List<String> {
        val recentBlockhash = rpcRepository.getRecentBlockhash()

        // STEP 3: prepare for topUp

        // todo: top up
        return emptyList()
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

    /*
    * Gets signature from transaction
    * */
    private fun getSignatures(
        transaction: Transaction,
        owner: Account,
        transferAuthorityAccount: Account
    ): SwapTransactionSignatures {

        val signers = listOf(owner, transferAuthorityAccount)
        transaction.sign(signers)

        val ownerSignatureData =
            transaction.findSignature(owner.publicKey)?.signature
        val transferAuthoritySignatureData =
            transaction.findSignature(transferAuthorityAccount.publicKey)?.signature

        if (ownerSignatureData.isNullOrEmpty() || transferAuthoritySignatureData.isNullOrEmpty()) {
            throw IllegalStateException("Invalid signatures")
        }

        val ownerSignature = Base58Utils.encode(ownerSignatureData.toByteArray())
        val transferAuthoritySignature = Base58Utils.encode(transferAuthoritySignatureData.toByteArray())

        return SwapTransactionSignatures(
            userAuthoritySignature = ownerSignature,
            transferAuthoritySignature = transferAuthoritySignature
        )
    }

    class FixedDestination(
        val destinationToken: TokenInfo,
        val userDestinationAccountOwnerAddress: PublicKey?,
        val needsCreateDestinationTokenAccount: Boolean
    )
}