package org.p2p.wallet.rpc.repository.pool

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.kits.Pool

interface RpcPoolRepository {
    suspend fun getPools(account: PublicKey): List<Pool.PoolInfo>
}