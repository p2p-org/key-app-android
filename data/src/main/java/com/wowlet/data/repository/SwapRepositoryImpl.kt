package com.wowlet.data.repository

import com.wowlet.data.datastore.PreferenceService
import com.wowlet.data.datastore.SwapRepository
import com.wowlet.entities.Constants
import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.kits.Pool.PoolInfo
import org.p2p.solanaj.kits.TokenSwap
import org.p2p.solanaj.programs.TokenSwapProgram.TokenSwapData
import org.p2p.solanaj.rpc.Cluster
import org.p2p.solanaj.rpc.RpcClient
import java.math.BigDecimal
import java.math.BigInteger

class SwapRepositoryImpl(
    private val rpcClient: RpcClient,
    private val preferenceService: PreferenceService
) : SwapRepository {

    override suspend fun swap(
        pool: PoolInfo,
        source: PublicKey,
        destination: PublicKey,
        slippage: Double,
        amountIn: BigInteger
    ): String {

        val activeWallet = preferenceService.getActiveWallet()
        val owner = Account.fromMnemonic(activeWallet?.phrase, "")

        val tokenSwap = TokenSwap(rpcClient, swapProgramId())

        return tokenSwap.swap(
            owner,
            pool,
            source,
            destination,
            slippage,
            amountIn
        )
    }

    override suspend fun getPool(source: PublicKey, destination: PublicKey): PoolInfo {
        return getPoolInfo(source.toBase58(), destination.toBase58())
    }

    override fun calculateSwapMinimumReceiveAmount(
        pool: PoolInfo,
        amount: BigInteger,
        slippage: Double
    ): BigInteger {
        val estimated =
            TokenSwap.calculateSwapEstimatedAmount(pool.tokenABalance, pool.tokenBBalance, amount)
        return TokenSwap.calculateSwapMinimumReceiveAmount(estimated, slippage)
    }

    override suspend fun getFee(
        amount: BigInteger,
        tokenSource: PublicKey,
        tokenDestination: PublicKey,
        pool: PoolInfo
    ): BigInteger {
        val swappedAmountWithFee = calculateAmountInOtherToken(
            tokenSource, pool.swapData, pool.tokenABalance.amount,
            pool.tokenBBalance.amount, amount, true
        )
        val swappedAmountWithoutFee = calculateAmountInOtherToken(
            tokenSource, pool.swapData, pool.tokenABalance.amount,
            pool.tokenBBalance.amount, amount, false
        )

        return swappedAmountWithoutFee.subtract(swappedAmountWithFee)
    }

    private fun getPoolInfo(
        sourceMint: String,
        destinationMint: String
    ): PoolInfo {
        val pools = Pool.getPools(rpcClient, swapProgramId())

        val pool = pools.first {
            val mintA = it.swapData.mintA.toBase58()
            val mintB = it.swapData.mintB.toBase58()
            (sourceMint == mintA && destinationMint == mintB) || (sourceMint == mintB && destinationMint == mintA)
        }

        // swap pool
        if (pool.swapData.mintB.toBase58() == sourceMint && pool.swapData.mintA.toBase58() == destinationMint) {
            pool.swapData.swapMintData()
            pool.swapData.swapTokenAccount()
            pool.swapTokenBalance()
            pool.swapTokenInfo()
        }

        return pool
    }

    private fun swapProgramId(): PublicKey = when (preferenceService.getSelectedCluster()) {
        Cluster.MAINNET -> PublicKey(Constants.MAIN_NET_PUBLIC_KEY)
        Cluster.DEVNET -> PublicKey(Constants.DEV_NET_PUBLIC_KEY)
        Cluster.TESTNET -> PublicKey(Constants.TEST_NET_PUBLIC_KEY)
        else -> PublicKey("")
    }

    private fun calculateAmountInOtherToken(
        tokenSource: PublicKey,
        swapData: TokenSwapData,
        tokenABalance: BigInteger,
        tokenBBalance: BigInteger,
        tokenInputAmount: BigInteger,
        includeFees: Boolean
    ): BigInteger {
        val isReverse = swapData.tokenAccountB.equals(tokenSource)
        val feeRatio: BigDecimal = BigDecimal(swapData.tradeFeeNumerator)
            .divide(BigDecimal(swapData.tradeFeeDenominator))
        val firstAmountInPool = if (isReverse) tokenBBalance else tokenABalance
        val secondAmountInPool = if (isReverse) tokenABalance else tokenBBalance
        val invariant = firstAmountInPool.multiply(secondAmountInPool)
        val newFromAmountInPool = firstAmountInPool.add(tokenInputAmount)
        val newToAmountInPool = invariant.divide(newFromAmountInPool)
        val grossToAmount = secondAmountInPool.subtract(newToAmountInPool)
        val fees: BigDecimal =
            if (includeFees) BigDecimal(grossToAmount).multiply(feeRatio) else BigDecimal.valueOf(0)
        return BigDecimal(grossToAmount).subtract(fees).toBigInteger()
    }
}