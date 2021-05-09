package com.p2p.wallet.common.di

import org.koin.dsl.module
import org.p2p.solanaj.rpc.Cluster
import org.p2p.solanaj.rpc.RpcClient

object CommonModule : InjectionModule {

    override fun create() = module {
        // todo: add switcher in settings
        single { RpcClient(Cluster.MAINNET) }
    }
}