package org.p2p.wallet.swap.repository

import org.p2p.wallet.swap.model.orca.OrcaPool
import kotlinx.coroutines.flow.Flow

interface OrcaSwapLocalRepository {
    fun setPools(pools: List<OrcaPool>)
    fun getPools(): List<OrcaPool>
    fun getPoolsFlow(): Flow<List<OrcaPool>>
    fun findPools(sourceMint: String, destinationMint: String): List<OrcaPool>
}