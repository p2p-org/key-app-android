package com.p2p.wallet.swap.orca.interactor

import com.p2p.wallet.R
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.main.model.Token
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import com.p2p.wallet.swap.orca.model.OrcaSwapResult
import com.p2p.wallet.swap.orca.model.ValidOrcaPool
import com.p2p.wallet.swap.orca.repository.OrcaSwapLocalRepository
import com.p2p.wallet.swap.orca.repository.OrcaSwapRepository
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.user.repository.UserLocalRepository
import com.p2p.wallet.utils.isZero
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.TokenTransaction
import org.p2p.solanaj.kits.transaction.SwapDetails.SWAP_PROGRAM_ID
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

private const val SWAP_TAG = "OrcaSwap"

class OrcaSwapInteractor(
    private val rpcRepository: RpcRepository,
    private val swapRepository: OrcaSwapRepository,
    private val swapLocalRepository: OrcaSwapLocalRepository,
    private val userInteractor: UserInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository
) {

    /*
     * TODO: optimize this one! remove [TokenSwap] move logic here
     * */
    suspend fun swap(
        request: OrcaSwapRequest,
        receivedAmount: BigDecimal,
        usdReceivedAmount: BigDecimal,
        tokenSymbol: String
    ): OrcaSwapResult {
        val accountAddressA = userInteractor.findAccountAddress(request.pool.sourceMint.toBase58())

        val owner = tokenKeyProvider.publicKey.toPublicKey()
        val associatedAddress = try {
            Timber.tag(SWAP_TAG).d("Searching for SPL token address")
            findSplTokenAddress(request.pool.destinationMint.toBase58(), owner)
        } catch (e: IllegalStateException) {
            Timber.tag(SWAP_TAG).d("Searching address failed, address is wrong")
            return OrcaSwapResult.Error(R.string.error_invalid_address)
        }

        /* If account is not found, create one */
        val accountInfo = rpcRepository.getAccountInfo(associatedAddress)
        val value = accountInfo?.value
        val associatedNotNeeded = value?.owner == TokenProgram.PROGRAM_ID.toString() && value.data != null

        val signature = swapRepository.swap(
            Account(tokenKeyProvider.secretKey),
            request,
            accountAddressA,
            associatedAddress,
            !associatedNotNeeded
        )

        return OrcaSwapResult.Success(signature, receivedAmount, usdReceivedAmount, tokenSymbol)
    }

    suspend fun loadAllPools() = withContext(Dispatchers.IO) {
        val swapProgramId = SWAP_PROGRAM_ID
        val pools = swapRepository.loadPools(swapProgramId)
        swapLocalRepository.setPools(pools)
    }

    fun getAllPools() = swapLocalRepository.getPools()

    suspend fun getAvailableDestinationTokens(source: Token.Active): List<Token> {
        val userTokens = userInteractor.getUserTokens()
        return swapLocalRepository.getPools()
            .filter { pool ->
                val hasDestination = pool.destinationMint.toBase58() == source.mintAddress
                val hasSource = pool.sourceMint.toBase58() == source.mintAddress
                hasDestination || hasSource
            }
            .mapNotNull { pool ->
                val mintA = pool.sourceMint.toBase58()
                val mintB = pool.destinationMint.toBase58()
                val mint = if (mintA == source.mintAddress) mintB else mintA
                val userToken = userTokens.find { it.mintAddress == mint }
                userToken ?: userInteractor.findTokenData(mint)
            }
            .distinctBy { it.mintAddress }
            .sortedBy { it is Token.Other }
    }

    suspend fun findValidPool(
        sourceMint: String,
        destinationMint: String
    ): ValidOrcaPool? = withContext(Dispatchers.IO) {
        swapLocalRepository.findPools(sourceMint, destinationMint)
            .mapNotNull {
                val balanceA = swapRepository.loadTokenBalance(it.tokenAccountA)
                val balanceB = swapRepository.loadTokenBalance(it.tokenAccountB)
                if (balanceA.amount.isZero() || balanceB.amount.isZero()) return@mapNotNull null
                ValidOrcaPool(it, balanceA, balanceB)
            }
            .firstOrNull()
    }

    @Throws(IllegalStateException::class)
    private suspend fun findSplTokenAddress(mintAddress: String, destinationAddress: PublicKey): PublicKey {
        val accountInfo = rpcRepository.getAccountInfo(destinationAddress)

        // detect if it is a direct token address
        val info = TokenTransaction.parseAccountInfoData(accountInfo, TokenProgram.PROGRAM_ID)
        if (info != null && userLocalRepository.findTokenData(info.mint.toBase58()) != null) {
            Timber.tag(SWAP_TAG).d("Token by mint was found. Continuing with direct address")
            return destinationAddress
        }

        // create associated token address
        val value = accountInfo?.value
        if (value == null || value.data?.get(0).isNullOrEmpty()) {
            Timber.tag(SWAP_TAG).d("No information found, creating associated token address")
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        // detect if destination address is already a SPLToken address
        if (info?.mint == destinationAddress) {
            Timber.tag(SWAP_TAG).d("Destination address is already an SPL Token address, returning")
            return destinationAddress
        }

        // detect if destination address is a SOL address
        if (info?.owner?.toBase58() == TokenProgram.PROGRAM_ID.toBase58()) {
            Timber.tag(SWAP_TAG).d("Destination address is SOL address. Getting the associated token address")

            // create associated token address
            return TokenTransaction.getAssociatedTokenAddress(mintAddress.toPublicKey(), destinationAddress)
        }

        throw IllegalStateException("Wallet address is not valid")
    }

    suspend fun getLamportsPerSignature(): BigInteger = rpcRepository.getFees(null)

    suspend fun getAccountMinForRentExemption(): BigInteger =
        rpcRepository
            .getMinimumBalanceForRentExemption(ACCOUNT_INFO_DATA_LENGTH.toLong())
            .toBigInteger()

    fun calculateNetworkFee(
        source: Token,
        destination: Token,
        lamportsPerSignature: BigInteger,
        minRentExemption: BigInteger
    ): BigInteger {
        // default fee
        var feeInLamports = lamportsPerSignature * BigInteger.valueOf(2L)

        // if token is native, a fee for creating wrapped SOL is needed
        if (source.isSOL) {
            feeInLamports += lamportsPerSignature
            feeInLamports += minRentExemption
        }

        // if destination wallet is selected
        // if destination wallet is a wrapped sol or not yet created a fee for creating it is needed
        if (destination.mintAddress == Token.WRAPPED_SOL_MINT || destination is Token.Other) {
            feeInLamports += minRentExemption
        }

        // fee relayer
        if (isFeeRelayerEnabled(source, destination)) {
            // fee for creating a SOL account
            feeInLamports += lamportsPerSignature
        }

        return feeInLamports
    }

    private fun isFeeRelayerEnabled(source: Token, destination: Token): Boolean {
        return !source.isSOL && !destination.isSOL
    }
}