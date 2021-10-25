package org.p2p.wallet.swap.interactor.orca

import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.utils.toPublicKey
import java.math.BigDecimal
import java.math.BigInteger

class OrcaAmountInteractor(
    private val tokenKeyProvider: TokenKeyProvider
) {

    fun calculateAmountInOtherToken(
        pool: OrcaPool,
        inputAmount: BigInteger,
        includeFees: Boolean,
        tokenABalance: AccountBalance,
        tokenBBalance: AccountBalance
    ): BigInteger {

        val tokenSource = tokenKeyProvider.publicKey.toPublicKey()
        val isReverse = pool.tokenAccountB.equals(tokenSource)

        val feeRatio = BigDecimal(pool.feeNumerator).divide(BigDecimal(pool.feeDenominator))

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
        val fees = if (includeFees) {
            BigDecimal(grossToAmount).multiply(feeRatio)
        } else {
            BigDecimal.ZERO
        }
        return BigDecimal(grossToAmount).subtract(fees).toBigInteger()
    }

    fun calculateEstimatedAmount(
        inputAmount: BigInteger,
        includeFees: Boolean,
        tokenABalance: AccountBalance,
        tokenBBalance: AccountBalance,
        pool: OrcaPool
    ): BigInteger {
        val feeDenominator = pool.feeDenominator
        val feeNumerator = if (includeFees) pool.feeNumerator else BigInteger.ZERO

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
        tokenABalance: AccountBalance,
        tokenBBalance: AccountBalance,
        pool: OrcaPool
    ): BigInteger {
        val amount = calculateEstimatedAmount(inputAmount, includesFees, tokenABalance, tokenBBalance, pool)
        return BigDecimal(amount).multiply(BigDecimal(1 - slippage)).toBigInteger()
    }

    fun calculateLiquidityFee(
        inputAmount: BigInteger,
        pool: OrcaPool,
        tokenABalance: AccountBalance,
        tokenBBalance: AccountBalance
    ): BigInteger {
        val feeDenominator = pool.feeDenominator
        val feeNumerator = pool.feeNumerator

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