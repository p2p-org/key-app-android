package com.p2p.wallet.swap.repository.orca

import com.p2p.wallet.swap.model.orca.OrcaPool
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class OrcaSwapInMemoryRepository : OrcaSwapLocalRepository {

    private val poolsInfoFlow = MutableStateFlow<List<OrcaPool>>(emptyList())

    override fun setPools(pools: List<OrcaPool>) {
        poolsInfoFlow.value = pools
    }

    override fun getPools(): List<OrcaPool> = poolsInfoFlow.value

    override fun getPoolsFlow(): Flow<List<OrcaPool>> = poolsInfoFlow

    override fun findPools(sourceMint: String, destinationMint: String): List<OrcaPool> {
        val pools = poolsInfoFlow.value
            .filter {
                val mintA = it.sourceMint.toBase58()
                val mintB = it.destinationMint.toBase58()
                (sourceMint == mintA && destinationMint == mintB) || (sourceMint == mintB && destinationMint == mintA)
            }
            .map { pool ->
                if (pool.destinationMint.toBase58() == sourceMint && pool.sourceMint.toBase58() == destinationMint) {
                    pool.swapData()
                } else {
                    pool
                }
            }

        return pools
    }
}