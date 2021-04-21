package com.p2p.wallet.main.repository

import org.p2p.solanaj.rpc.RpcClient

class MainRepositoryImpl(
    private val client: RpcClient
) : MainRepository