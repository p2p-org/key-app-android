package com.p2p.wallet.swap.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.p2p.solanaj.kits.Pool

class SwapInMemoryRepository : SwapLocalRepository {

    private val poolsInfoFlow = MutableStateFlow<List<Pool.PoolInfo>>(emptyList())

    override fun setPools(pools: List<Pool.PoolInfo>) {
        poolsInfoFlow.value = pools
    }

    override fun getPools(): List<Pool.PoolInfo> = poolsInfoFlow.value

    override fun getPoolsFlow(): Flow<List<Pool.PoolInfo>> = poolsInfoFlow

    override suspend fun getPoolInfo(sourceMint: String, destinationMint: String): Pool.PoolInfo? =
        withContext(Dispatchers.IO) {
            val pool = poolsInfoFlow.value.firstOrNull {
                val mintA = it.swapData.mintA.toBase58()
                val mintB = it.swapData.mintB.toBase58()
                (sourceMint == mintA && destinationMint == mintB) || (sourceMint == mintB && destinationMint == mintA)
            }

            if (pool?.swapData?.mintB?.toBase58() == sourceMint &&
                pool.swapData?.mintA?.toBase58() == destinationMint
            ) {
                pool.swapData?.swapMintData()
                pool.swapData?.swapTokenAccount()
            }

            return@withContext pool
        }
}