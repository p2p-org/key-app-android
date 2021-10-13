package com.p2p.wallet.swap.orca.repository

import com.p2p.wallet.swap.orca.model.OrcaPool
import kotlinx.coroutines.flow.Flow

interface OrcaSwapLocalRepository {
    fun setPools(pools: List<OrcaPool>)
    fun getPools(): List<OrcaPool>
    fun getPoolsFlow(): Flow<List<OrcaPool>>
    fun findPools(sourceMint: String, destinationMint: String): List<OrcaPool>
}