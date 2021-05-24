package com.p2p.wallet.swap.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.solanaj.kits.Pool

interface SwapLocalRepository {
    fun setPools(pools: List<Pool.PoolInfo>)
    fun getPools(): List<Pool.PoolInfo>
    fun getPoolsFlow(): Flow<List<Pool.PoolInfo>>
    suspend fun getPoolInfo(sourceMint: String, destinationMint: String): Pool.PoolInfo?
}