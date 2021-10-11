package com.p2p.wallet.swap.orca.interactor

import com.p2p.wallet.main.model.Token
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.rpc.repository.RpcRepository
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import com.p2p.wallet.swap.orca.model.OrcaSwapResult
import com.p2p.wallet.swap.orca.repository.OrcaSwapLocalRepository
import com.p2p.wallet.swap.orca.repository.OrcaSwapRepository
import com.p2p.wallet.user.interactor.UserInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.transaction.SwapDetails.SWAP_PROGRAM_ID
import org.p2p.solanaj.model.types.TokenAccountBalance
import org.p2p.solanaj.programs.TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH
import java.math.BigDecimal
import java.math.BigInteger

class OrcaSwapInteractor(
    private val rpcRepository: RpcRepository,
    private val swapRepository: OrcaSwapRepository,
    private val swapLocalRepository: OrcaSwapLocalRepository,
    private val userInteractor: UserInteractor,
    private val secretKeyInteractor: SecretKeyInteractor
) {

    suspend fun loadAllPools() {
        val swapProgramId = SWAP_PROGRAM_ID
        val pools = swapRepository.loadPoolInfoList(swapProgramId)
        swapLocalRepository.setPools(pools)
    }

    fun getAllPools() = swapLocalRepository.getPools()

    suspend fun getAvailableDestinationTokens(source: Token.Active): List<Token> {
        val userTokens = userInteractor.getUserTokens()
        return swapLocalRepository.getPools()
            .filter { pool ->
                pool.mintB.toBase58() == source.mintAddress || pool.mintA.toBase58() == source.mintAddress
            }
            .mapNotNull { pool ->
                val mintA = pool.mintA.toBase58()
                val mintB = pool.mintB.toBase58()
                val mint = if (mintA == source.mintAddress) mintB else mintA
                val userToken = userTokens.find { it.mintAddress == mint }
                userToken ?: userInteractor.findTokenData(mint)
            }
            .distinctBy { it.mintAddress }
            .sortedBy { it is Token.Other }
    }

    suspend fun findPool(sourceMint: String, destinationMint: String): Pool.PoolInfo? = withContext(Dispatchers.IO) {
        val allPools = swapLocalRepository.getPools()
        val pool = allPools.lastOrNull {
            val mintA = it.swapData.mintA.toBase58()
            val mintB = it.swapData.mintB.toBase58()
            sourceMint == mintA && destinationMint == mintB || sourceMint == mintB && destinationMint == mintA
        } ?: return@withContext null

        if (pool.swapData.mintB.toBase58() == sourceMint && pool.swapData.mintA.toBase58() == destinationMint) {
            pool.swapData.swapMintData()
            pool.swapData.swapTokenAccount()
        }

        return@withContext pool
    }

    suspend fun loadTokenBalance(publicKey: PublicKey): TokenAccountBalance =
        swapRepository.loadTokenBalance(publicKey)

    suspend fun swap(
        request: OrcaSwapRequest,
        receivedAmount: BigDecimal,
        usdReceivedAmount: BigDecimal,
        tokenSymbol: String
    ): OrcaSwapResult {
        val accountAddressA = userInteractor.findAccountAddress(request.pool.mintA.toBase58())
        val accountAddressB = userInteractor.findAccountAddress(request.pool.mintB.toBase58())
        val keys = secretKeyInteractor.getSecretKeys()
        val path = secretKeyInteractor.getCurrentDerivationPath()
        val signature = swapRepository.swap(path, keys, request, accountAddressA, accountAddressB)
        return OrcaSwapResult.Success(signature, receivedAmount, usdReceivedAmount, tokenSymbol)
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

    // MARK: - Helpers
    private fun isFeeRelayerEnabled(source: Token, destination: Token): Boolean {
        return !source.isSOL && !destination.isSOL
    }
}