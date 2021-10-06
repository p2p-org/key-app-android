package com.p2p.wallet.swap.orca.interactor

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.swap.orca.model.OrcaSwapRequest
import com.p2p.wallet.swap.orca.model.OrcaSwapResult
import com.p2p.wallet.swap.orca.repository.OrcaSwapLocalRepository
import com.p2p.wallet.swap.orca.repository.OrcaSwapRepository
import com.p2p.wallet.user.interactor.UserInteractor
import com.p2p.wallet.utils.toPublicKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.transaction.SwapDetails.KNOWN_SWAP_PROGRAM_IDS
import org.p2p.solanaj.model.types.TokenAccountBalance
import java.math.BigDecimal
import java.math.BigInteger

class OrcaSwapInteractor(
    private val swapRepository: OrcaSwapRepository,
    private val swapLocalRepository: OrcaSwapLocalRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val userInteractor: UserInteractor,
    private val secretKeyInteractor: SecretKeyInteractor
) {

    suspend fun loadAllPools() {
        val swapProgramId = KNOWN_SWAP_PROGRAM_IDS.first()
        val pools = swapRepository.loadPoolInfoList(swapProgramId)
        swapLocalRepository.setPools(pools)
    }

    fun getAllPools() =
        swapLocalRepository.getPools()

    suspend fun findPool(sourceMint: String, destinationMint: String): Pool.PoolInfo? = withContext(Dispatchers.IO) {
        val allPools = swapLocalRepository.getPools()
        val pool = allPools.find {
            val mintA = it.swapData.mintA.toBase58()
            val mintB = it.swapData.mintB.toBase58()
            (sourceMint == mintA && destinationMint == mintB) || (sourceMint == mintB && destinationMint == mintA)
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

    fun calculateFee(
        pool: Pool.PoolInfo,
        inputAmount: BigInteger,
        tokenABalance: TokenAccountBalance,
        tokenBBalance: TokenAccountBalance
    ): BigInteger {
        val swappedAmountWithFee = calculateAmountInOtherToken(
            pool, inputAmount, true, tokenABalance, tokenBBalance
        )

        val swappedAmountWithoutFee = calculateAmountInOtherToken(
            pool, inputAmount, false, tokenABalance, tokenBBalance
        )

        return swappedAmountWithoutFee.subtract(swappedAmountWithFee)
    }

    fun calculateMinReceive(
        balanceA: TokenAccountBalance,
        balanceB: TokenAccountBalance,
        amount: BigInteger,
        slippage: Double
    ): BigInteger {
        val add = balanceA.amount.add(amount)
        val estimated =
            if (add.compareTo(BigInteger.ZERO) != 0) balanceB.amount.multiply(amount).divide(add) else BigInteger.ZERO
        return BigDecimal(estimated).multiply(BigDecimal(1 - slippage)).toBigInteger()
    }

    fun calculateAmountInOtherToken(
        pool: Pool.PoolInfo,
        inputAmount: BigInteger,
        withFee: Boolean,
        tokenABalance: TokenAccountBalance,
        tokenBBalance: TokenAccountBalance
    ): BigInteger {

        val tokenSource = tokenKeyProvider.publicKey.toPublicKey()
        val isReverse = pool.tokenAccountB.equals(tokenSource)

        val feeRatio = BigDecimal(pool.tradeFeeNumerator).divide(BigDecimal(pool.tradeFeeDenominator))

        val firstAmountInPool = if (isReverse) tokenBBalance.amount else tokenABalance.amount
        val secondAmountInPool = if (isReverse) tokenABalance.amount else tokenBBalance.amount

        val invariant = firstAmountInPool.multiply(secondAmountInPool)
        val newFromAmountInPool = firstAmountInPool.add(inputAmount)
        val newToAmountInPool = if (newFromAmountInPool.compareTo(BigInteger.ZERO) != 0) {
            invariant.divide(newFromAmountInPool)
        } else {
            BigInteger.ZERO
        }
        val grossToAmount = secondAmountInPool.subtract(newToAmountInPool)
        val fees = if (withFee) {
            BigDecimal(grossToAmount).multiply(feeRatio)
        } else {
            BigDecimal.valueOf(0)
        }
        return BigDecimal(grossToAmount).subtract(fees).toBigInteger()
    }
}