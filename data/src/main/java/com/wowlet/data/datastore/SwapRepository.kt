package com.wowlet.data.datastore

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool
import java.math.BigInteger

interface SwapRepository {

    suspend fun swap(
        pool: Pool.PoolInfo,
        source: PublicKey,
        destination: PublicKey,
        slippage: Double,
        amountIn: BigInteger
    ): String

    suspend fun getFee(
        amount: BigInteger,
        tokenSource: PublicKey,
        tokenDestination: PublicKey,
        pool: Pool.PoolInfo
    ): BigInteger

    suspend fun getPool(source: PublicKey, destination: PublicKey): Pool.PoolInfo

    fun calculateSwapMinimumReceiveAmount(pool: Pool.PoolInfo, amount: BigInteger, slippage: Double): BigInteger
}