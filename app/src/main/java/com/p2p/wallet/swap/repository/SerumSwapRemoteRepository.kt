package com.p2p.wallet.swap.repository

import com.p2p.wallet.rpc.repository.RpcRepository

class SerumSwapRemoteRepository(
    private val rpcRepository: RpcRepository
) : SerumSwapRepository {

}