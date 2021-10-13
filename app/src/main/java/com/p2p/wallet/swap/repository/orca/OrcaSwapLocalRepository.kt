package com.p2p.wallet.swap.repository.orca

import com.p2p.wallet.swap.model.orca.OrcaPool
import kotlinx.coroutines.flow.Flow

interface OrcaSwapLocalRepository {
    fun setPools(pools: List<OrcaPool>)
    fun getPools(): List<OrcaPool>
    fun getPoolsFlow(): Flow<List<OrcaPool>>
    fun findPools(sourceMint: String, destinationMint: String): List<OrcaPool>
}