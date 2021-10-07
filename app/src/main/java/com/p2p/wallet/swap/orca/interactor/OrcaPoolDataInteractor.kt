package com.p2p.wallet.swap.orca.interactor

import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.solanaj.kits.Pool
import org.p2p.solanaj.model.types.TokenAccountBalance
import java.math.BigDecimal
import java.math.BigInteger

class OrcaPoolDataInteractor(
    private val tokenKeyProvider: TokenKeyProvider
) {

    fun calculateEstimatedAmount(
        inputAmount: BigInteger,
        includeFees: Boolean,
        tokenABalance: TokenAccountBalance,
        tokenBBalance: TokenAccountBalance,
        pool: Pool.PoolInfo
    ): BigInteger {
        val feeDenominator = pool.tradeFeeDenominator
        val feeNumerator = if (includeFees) pool.tradeFeeNumerator else BigInteger.ZERO

        val amountA = tokenABalance.amount
        val amountB = tokenBBalance.amount
        val numerator = amountB * inputAmount * (feeDenominator - feeNumerator)
        val denominator = (amountA + inputAmount) * feeDenominator

        if (denominator.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO
        }

        return numerator / denominator
    }

    fun calculateMinReceive(
        inputAmount: BigInteger,
        slippage: Double,
        includesFees: Boolean,
        tokenABalance: TokenAccountBalance,
        tokenBBalance: TokenAccountBalance,
        pool: Pool.PoolInfo
    ): BigInteger {
        val amount = calculateEstimatedAmount(inputAmount, includesFees, tokenABalance, tokenBBalance, pool)
        return BigDecimal(amount).multiply(BigDecimal(1 - slippage)).toBigInteger()
    }

    fun calculateLiquidityFee(
        inputAmount: BigInteger,
        pool: Pool.PoolInfo,
        tokenABalance: TokenAccountBalance,
        tokenBBalance: TokenAccountBalance
    ): BigInteger {
        val feeDenominator = pool.tradeFeeDenominator
        val feeNumerator = pool.tradeFeeNumerator

        val aBalance = tokenABalance.amount
        val bBalance = tokenBBalance.amount
        val numerator = bBalance * inputAmount * feeNumerator
        val denominator = (aBalance + inputAmount) * feeDenominator

        if (denominator.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO
        }

        return numerator.divide(denominator)
    }
}