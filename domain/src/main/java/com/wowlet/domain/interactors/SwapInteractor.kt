package com.wowlet.domain.interactors

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import java.math.BigDecimal
import java.math.BigInteger

interface SwapInteractor {

    fun getAroundToCurrencyValue(amount: String, walletBinds: Double, isInCryptoCurrency: Boolean): Double
    fun getAmountInConvertingToken(amount: String, from: Double, to: Double): Double
    fun getTokenPerToken(from: Double, to: Double): Double

    fun  getMinimumReceiveAmount(pool: Pool.PoolInfo, amount: BigInteger, slippage: Double): BigInteger

    suspend fun swap(
       source: String?,
       destination: String?,
       amount: BigInteger,
       slippage: Double
   ): String

    suspend  fun getFee(
        amount: BigInteger,
        tokenSource: String?,
        tokenDestination: String?,
        pool: Pool.PoolInfo
    ): BigDecimal
    
    suspend fun getPool(source: PublicKey, destination: PublicKey): Pool.PoolInfo

}