package org.p2p.wallet.swap.model.orca

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.programs.TokenSwapProgram
import org.p2p.wallet.swap.model.AccountBalance
import org.p2p.core.utils.divideSafe
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.isZeroOrLess
import org.p2p.wallet.utils.toPublicKey
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

typealias OrcaPools = MutableMap<String, OrcaPool> // [poolId: string]: PoolConfig
typealias OrcaPoolsPair = MutableList<OrcaPool>

private const val ACTUAL_PROGRAM_ID = 1

data class OrcaPool(
    val account: PublicKey,
    val authority: PublicKey,
    val nonce: Int,
    val poolTokenMint: PublicKey,
    val tokenAccountA: PublicKey,
    val tokenAccountB: PublicKey,
    val feeAccount: PublicKey,
    val hostFeeAccount: PublicKey? = null,
    val feeNumerator: BigInteger,
    val feeDenominator: BigInteger,
    val ownerTradeFeeNumerator: BigInteger,
    val ownerTradeFeeDenominator: BigInteger,
    val ownerWithdrawFeeNumerator: BigInteger,
    val ownerWithdrawFeeDenominator: BigInteger,
    val hostFeeNumerator: BigInteger,
    val hostFeeDenominator: BigInteger,
    val tokenAName: String,
    val tokenBName: String,
    val curveType: String,
    val deprecated: Boolean = false,
    val programVersion: Int?,
    val amp: BigInteger?,

    // lazy load
    var tokenABalance: AccountBalance? = null,
    var tokenBBalance: AccountBalance? = null
) {

    var isStable: Boolean? = null

    val reversed: OrcaPool
        get() {
            return this.copy(
                tokenAccountA = tokenAccountB,
                tokenAccountB = tokenAccountA,
                tokenAName = tokenBName,
                tokenBName = tokenAName,
                tokenABalance = tokenBBalance,
                tokenBBalance = tokenABalance
            )
        }

    val swapProgramId: PublicKey
        get() = TokenSwapProgram.getSwapProgramId(programVersion ?: ACTUAL_PROGRAM_ID).toPublicKey()

    fun getOutputAmount(
        inputAmount: BigInteger
    ): BigInteger {
        val fees = getFee(inputAmount)
        val inputAmountLessFee = inputAmount - fees
        return getOutputAmountInternal(inputAmountLessFee)
    }

    fun getInputAmount(
        estimatedAmount: BigInteger
    ): BigInteger? {
        val poolInputAmount = tokenABalance?.amount
        val poolOutputAmount = tokenBBalance?.amount

        if (poolInputAmount == null || poolOutputAmount == null)
            throw IllegalStateException("Account balances not found")

        if (estimatedAmount > poolOutputAmount) {
            Timber.e("Estimated amount is too high")
            return null
        }

        when (curveType) {
            STABLE -> {
                if (amp == null) throw IllegalStateException("Amp doesn't exist in pool config")
                val inputAmountLessFee = computeInputAmount(estimatedAmount, poolInputAmount, poolOutputAmount, amp)

                val stableInputAmount = inputAmountLessFee * feeDenominator / feeDenominator - feeNumerator
                return if (stableInputAmount.isZeroOrLess()) BigInteger.ZERO else stableInputAmount
            }
            CONSTANT_PRODUCT -> {
                val invariant = poolInputAmount * poolOutputAmount

                val newPoolInputAmount = ceilingDivision(invariant, poolOutputAmount - estimatedAmount).first
                val inputAmountLessFee = newPoolInputAmount - poolInputAmount

                val feeRatioNumerator: BigInteger
                val feeRatioDenominator: BigInteger

                if (ownerTradeFeeDenominator.isZero()) {
                    feeRatioNumerator = feeDenominator
                    feeRatioDenominator = feeDenominator - feeNumerator
                } else {
                    feeRatioNumerator = feeDenominator * ownerTradeFeeDenominator
                    feeRatioDenominator =
                        feeDenominator * ownerTradeFeeDenominator -
                        (feeNumerator * ownerTradeFeeDenominator) - (ownerTradeFeeNumerator * feeDenominator)
                }

                val inputAmount = inputAmountLessFee * feeRatioNumerator / feeRatioDenominator
                return if (inputAmount.isZeroOrLess()) BigInteger.ZERO else inputAmount
            }
            else ->
                return null
        }
    }

    fun calculatingFees(inputAmount: BigInteger): BigInteger {
        val inputFees = getFee(inputAmount)
        return getOutputAmountInternal(inputFees)
    }

    fun getMinimumAmountOut(
        inputAmount: BigInteger,
        slippage: Double
    ): BigInteger? {
        val estimatedOutputAmount = getOutputAmount(inputAmount)
        val amount = BigDecimal(estimatedOutputAmount.toDouble() * (1 - slippage))
        return amount.toBigInteger()
    }

    fun getInputAmount(
        minimumReceiveAmount: BigInteger,
        slippage: Double
    ): BigInteger? {
        if (slippage == 1.0) return null

        val estimatedAmount = BigDecimal(minimumReceiveAmount.toDouble() / (1 - slippage))
        return getInputAmount(estimatedAmount.toBigInteger())
    }

    // / baseOutputAmount is the amount the user would receive if fees are included and slippage is excluded.
    private fun getBaseOutputAmount(
        inputAmount: BigInteger
    ): BigInteger? {
        val poolInputAmount = tokenABalance?.amount
        val poolOutputAmount = tokenBBalance?.amount

        if (poolInputAmount == null || poolOutputAmount == null)
            throw IllegalStateException("Account balances not found")

        val fees = getFee(inputAmount)
        val inputAmountLessFee = inputAmount - fees

        return when (curveType) {
            STABLE -> {
                if (amp == null) throw IllegalStateException("Amp doesn't exist in pool config")
                computeBaseOutputAmount(
                    inputAmountLessFee,
                    poolInputAmount,
                    poolOutputAmount,
                    amp
                )
            }
            CONSTANT_PRODUCT ->
                inputAmountLessFee * poolOutputAmount / poolInputAmount
            else ->
                null
        }
    }

    // / price impact
    fun getPriceImpact(inputAmount: BigInteger): BigDecimal? {
        val baseOutputAmount = getBaseOutputAmount(inputAmount) ?: return null

        val inputAmountDecimal = inputAmount.fromLamports(decimals = 0).toDouble()
        val baseOutputAmountDecimal = baseOutputAmount.fromLamports(decimals = 0).toDouble()

        return BigDecimal((baseOutputAmountDecimal - inputAmountDecimal) / baseOutputAmountDecimal * 100.0)
    }

    // MARK: - Helpers
    private fun getFee(inputAmount: BigInteger): BigInteger {
        if (curveType != STABLE && curveType != CONSTANT_PRODUCT) throw IllegalStateException("Curve type unknown")
        val tradingFee = computeFee(inputAmount, feeNumerator, feeDenominator)
        val ownerFee = computeFee(inputAmount, ownerTradeFeeNumerator, ownerTradeFeeDenominator)
        return tradingFee + ownerFee
    }

    private fun getOutputAmountInternal(inputAmount: BigInteger): BigInteger {
        val poolInputAmount = tokenABalance?.amount
        val poolOutputAmount = tokenBBalance?.amount

        if (poolInputAmount == null || poolOutputAmount == null) {
            throw IllegalStateException("Account balances not found")
        }

        when (curveType) {
            STABLE -> {
                val amp = amp ?: throw IllegalStateException("Amp doesn't exist in pool config")
                return computeOutputAmount(
                    inputAmount = inputAmount,
                    inputPoolAmount = poolInputAmount,
                    outputPoolAmount = poolOutputAmount,
                    amp = amp
                )
            }
            CONSTANT_PRODUCT -> {
                val invariant = poolInputAmount * poolOutputAmount
                val newPoolOutputAmount = ceilingDivision(invariant, poolInputAmount + inputAmount).first
                return poolOutputAmount - newPoolOutputAmount
            }
            else -> throw IllegalStateException("Curve type is unknown $curveType")
        }
    }

    private fun computeFee(baseAmount: BigInteger, feeNumerator: BigInteger, feeDenominator: BigInteger): BigInteger {
        if (feeNumerator.isZero()) {
            return BigInteger.ZERO
        }

        return baseAmount.multiply(feeNumerator).divideSafe(feeDenominator)
    }

    companion object {
        private const val N_COINS = 2
        private const val N_COINS_SQUARED = 4
        private const val STABLE = "Stable"
        private const val CONSTANT_PRODUCT = "ConstantProduct"

        fun OrcaPoolsPair.getOutputAmount(
            inputAmount: BigInteger
        ): BigInteger? {
            if (this.isEmpty()) return null

            val pool0 = this[0]
            val estimatedAmountOfPool0 = pool0.getOutputAmount(inputAmount)

            // direct
            return if (size == 1) {
                estimatedAmountOfPool0
            }

            // transitive
            else {
                val pool1 = this[1]
                pool1.getOutputAmount(estimatedAmountOfPool0)
            }
        }

        fun OrcaPoolsPair.getInputAmount(
            minimumAmountOut: BigInteger,
            slippage: Double
        ): BigInteger? {
            if (isEmpty()) return null

            val pool0 = this[0]
            // direct
            if (size == 1) {
                return pool0.getInputAmount(minimumAmountOut, slippage)
            } else {
                val pool1 = this[1]
                val inputAmountPool1 = pool1.getInputAmount(minimumAmountOut, slippage) ?: return null
                return pool0.getInputAmount(inputAmountPool1, slippage)
            }
        }

        fun OrcaPoolsPair.getInputAmount(
            estimatedAmount: BigInteger
        ): BigInteger? {
            if (this.isEmpty()) return null

            // direct
            if (size == 1) {
                val pool0 = this[0]
                val inputAmountOfPool0 = pool0.getInputAmount(estimatedAmount) ?: return null
                return inputAmountOfPool0
            }
            // transitive
            else {
                val pool1 = this[1]
                val inputAmountOfPool1 = pool1.getInputAmount(estimatedAmount) ?: return null
                val pool0 = this[0]

                return pool0.getInputAmount(inputAmountOfPool1)
            }
        }

        fun OrcaPoolsPair.getMinimumAmountOut(
            inputAmount: BigInteger,
            slippage: Double
        ): BigInteger? {
            if (size == 0) return null
            val pool0 = this[0]
            // direct
            return if (size == 1) {
                pool0.getMinimumAmountOut(inputAmount, slippage) ?: return null
            } else {
                // transitive
                val outputAmountOfPool0 = pool0.getOutputAmount(inputAmount)
                val pool1 = this[1]
                pool1.getMinimumAmountOut(outputAmountOfPool0, slippage) ?: return null
            }
        }

        fun OrcaPoolsPair.getIntermediaryToken(
            inputAmount: BigInteger,
            slippage: Double
        ): OrcaInterTokenInfo? {
            if (size <= 1) return null
            val pool0 = this[0]
            return OrcaInterTokenInfo(
                tokenName = pool0.tokenBName,
                outputAmount = pool0.getOutputAmount(inputAmount),
                minAmountOut = pool0.getMinimumAmountOut(inputAmount, slippage),
                isStableSwap = this[1].isStable == true
            )
        }

        // Take the derivative of the invariant function over x
        private fun computeBaseOutputAmount(
            inputAmount: BigInteger,
            inputPoolAmount: BigInteger,
            outputPoolAmount: BigInteger,
            amp: BigInteger
        ): BigInteger {
            val leverage = amp * N_COINS.toBigInteger()
            val invariant = computeD(leverage, inputPoolAmount, outputPoolAmount)
            val a = amp * BigInteger.valueOf(16L)
            val b = a
            val c = invariant * BigInteger.valueOf(4) - (invariant * amp * BigInteger.valueOf(16))

            val numerator =
                (a * BigInteger.valueOf(2) * inputPoolAmount + (b * outputPoolAmount) + c) * outputPoolAmount

            val denominator =
                (a * inputPoolAmount + (b * BigInteger.valueOf(2) * outputPoolAmount + c)) * inputPoolAmount

            return inputAmount * numerator.divideSafe(denominator)
        }

        // A * sum(x_i) * n**n + D = A * D * n**n + D**(n+1) / (n**n * prod(x_i))
        private fun computeD(leverage: BigInteger, amountA: BigInteger, amountB: BigInteger): BigInteger {
            val amountATimesN = amountA * N_COINS.toBigInteger() + BigInteger.ONE
            val amountBTimesN = amountB * N_COINS.toBigInteger() + BigInteger.ONE
            val sumX = amountA + amountB

            if (sumX.isZero()) {
                return BigInteger.ZERO
            }

            var dPrevious: BigInteger
            var d = sumX

            for (i in 0 until 31) {
                var dProduct = d
                dProduct = dProduct * d / amountATimesN
                dProduct = dProduct * d / amountBTimesN
                dPrevious = d
                d = calculateStep(d, leverage, sumX, dProduct)
                if (d == dPrevious) {
                    break
                }
            }

            return d
        }

        // d = (leverage * sum_x + d_product * n_coins) * initial_d / ((leverage - 1) * initial_d + (n_coins + 1) * d_product)
        private fun calculateStep(
            initialD: BigInteger,
            leverage: BigInteger,
            sumX: BigInteger,
            dProduct: BigInteger
        ): BigInteger {
            val leverageMul = leverage * sumX
            val dPMul = dProduct * N_COINS.toBigInteger()

            val leverageVal = (leverageMul + dPMul) * initialD

            val leverageSub = initialD * (leverage - BigInteger.ONE)
            val nCoinsSum = dProduct * (N_COINS.toBigInteger() + BigInteger.ONE)

            val rVal = leverageSub + nCoinsSum

            return leverageVal / rVal
        }

        // Compute swap amount `y` in proportion to `x`
        // Solve for y:
        // y**2 + y * (sum' - (A*n**n - 1) * D / (A * n**n)) = D ** (n + 1) / (n ** (2 * n) * prod' * A)
        // y**2 + b*y = c
        private fun computeOutputAmountInternal(
            leverage: BigInteger,
            newInputAmount: BigInteger,
            d: BigInteger
        ): BigInteger {
            val c = d.pow(N_COINS + 1) / (newInputAmount * N_COINS_SQUARED.toBigInteger() * leverage)

            val b = newInputAmount + (d / leverage)

            var yPrevious: BigInteger
            var y = d

            for (i in 0 until 32) {
                yPrevious = y
                y = ((y.pow(2)) + c) / ((y * 2.toBigInteger()) + b - d)
                if (y == yPrevious) {
                    break
                }
            }

            return y
        }

        private fun computeInputAmount(
            outputAmount: BigInteger,
            inputPoolAmount: BigInteger,
            outputPoolAmount: BigInteger,
            amp: BigInteger
        ): BigInteger {
            val leverage = amp * N_COINS.toBigInteger()
            val newOutputPoolAmount = outputPoolAmount - outputAmount
            val d = computeD(
                leverage,
                outputPoolAmount,
                inputPoolAmount
            )
            val newInputPoolAmount = computeOutputAmountInternal(
                leverage,
                newOutputPoolAmount,
                d
            )
            val inputAmount = newInputPoolAmount - inputPoolAmount
            return inputAmount
        }

        /**
         * @return Pair<quotient, divisor>)
         */
        private fun ceilingDivision(
            dividend: BigInteger,
            divisor: BigInteger
        ): Pair<BigInteger, BigInteger> {
            if (divisor.isZero()) {
                return BigInteger.ZERO to divisor
            }

            var resultDivisor = divisor
            var resultQuotient = dividend / resultDivisor
            if (resultQuotient.isZero()) {
                return BigInteger.ZERO to resultDivisor
            }

            var remainder = dividend % resultDivisor
            if (remainder > BigInteger.ZERO) {
                resultQuotient += BigInteger.ONE
                resultDivisor = dividend / resultQuotient
                remainder = dividend / resultQuotient
                if (remainder > BigInteger.ZERO) {
                    resultDivisor += BigInteger.ONE
                }
            }

            return resultQuotient to resultDivisor
        }

        private fun computeOutputAmount(
            inputAmount: BigInteger,
            inputPoolAmount: BigInteger,
            outputPoolAmount: BigInteger,
            amp: BigInteger
        ): BigInteger {
            val leverage = amp * N_COINS.toBigInteger()
            val newInputPoolAmount = inputAmount + inputPoolAmount
            val d = computeD(leverage, inputPoolAmount, outputPoolAmount)

            val newOutputPoolAmount = computeOutputAmountInternal(leverage, newInputPoolAmount, d)
            val outputAmount = outputPoolAmount - newOutputPoolAmount
            return outputAmount
        }
    }
}
