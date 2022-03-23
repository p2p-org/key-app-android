package org.p2p.wallet.rpc.repository.blockhash

import org.p2p.solanaj.model.types.RecentBlockhash

interface RpcBlockhashRepository {
    suspend fun getRecentBlockhash(): RecentBlockhash
}
