package com.wowlet.domain.usecases

import com.wowlet.data.datastore.SwapRepository
import com.wowlet.domain.interactors.SwapInteractor
import com.wowlet.entities.Constants
import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.math.pow

class SwapUseCase(private val swapRepository: SwapRepository) : SwapInteractor {

    override fun getAroundToCurrencyValue(
        amount: String,
        walletBinds: Double,
        isInCryptoCurrency: Boolean
    ): Double {
        val amountAsDouble: Double = if (amount == "" || amount == ".") 0.0 else amount.toDouble()
        return if (isInCryptoCurrency) {
            amountAsDouble.times(walletBinds)
        } else {
            amountAsDouble.div(walletBinds)
        }
    }

    override fun getAmountInConvertingToken(amount: String, from: Double, to: Double): Double {
        // 1 <from token> = currencyInFrom <to token>
        val currencyInFrom: Double = from.div(to)
        val amountAsDouble: Double = if (amount == "" || amount == ".") 0.0 else amount.toDouble()
        return amountAsDouble.times(currencyInFrom)
    }

    override fun getTokenPerToken(from: Double, to: Double): Double {
        return to.div(from)
    }

    override suspend fun swap(
        source: String?,
        destination: String?,
        amount: BigInteger,
        slippage: Double,
        poolInfo: Pool.PoolInfo
    ): String {
        val fromMintAddress = if (source == "SOLMINT") Constants.SWAP_SOL else source
        val toMintAddress = if (destination == "SOLMINT") Constants.SWAP_SOL else destination
        return swapRepository.swap(
            poolInfo,
            PublicKey(fromMintAddress),
            PublicKey(toMintAddress),
            slippage,
            amount
        )
    }

    override suspend fun getPool(source: PublicKey, destination: PublicKey): Pool.PoolInfo =
        swapRepository.getPool(source, destination)

    override suspend fun getFee(
        amount: BigInteger, tokenSource: String?, tokenDestination: String?, pool: Pool.PoolInfo
    ): BigDecimal {
        val fromMintAddress = if (tokenSource == "SOLMINT") Constants.SWAP_SOL else tokenSource
        val toMintAddress =
            if (tokenDestination == "SOLMINT") Constants.SWAP_SOL else tokenDestination
        val fee =
            swapRepository.getFee(amount, PublicKey(fromMintAddress), PublicKey(toMintAddress), pool)
        val feeValue = (fee.toInt() / 10.0.pow(9.0))
        return BigDecimal(feeValue).setScale(6, RoundingMode.HALF_EVEN)
    }

    override fun getMinimumReceiveAmount(pool: Pool.PoolInfo, amount: BigInteger, slippage: Double): BigInteger {
        return swapRepository.calculateSwapMinimumReceiveAmount(pool, amount, slippage)
    }
}