package org.p2p.wallet.bridge

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import org.p2p.core.rpc.RpcApi
import org.p2p.wallet.bridge.api.mapper.BridgeMapper
import org.p2p.wallet.bridge.api.mapper.BridgeServiceErrorMapper
import org.p2p.wallet.bridge.repository.BridgeRemoteRepository
import org.p2p.wallet.bridge.repository.BridgeRepository
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.rpc.RPC_JSON_QUALIFIER

object BridgeModule : InjectionModule {
    const val BRIDGE_RETROFIT_QUALIFIER = "BRIDGE_RETROFIT_QUALIFIER"

    override fun create(): Module = module {
        single {
            val api = get<Retrofit>(named(BRIDGE_RETROFIT_QUALIFIER)).create(RpcApi::class.java)
            BridgeRemoteRepository(
                api = api,
                gson = get(named(RPC_JSON_QUALIFIER)),
                errorMapper = BridgeServiceErrorMapper(),
                urlProvider = get()
            )
        } bind BridgeRepository::class
        factoryOf(::BridgeMapper)
    }
}
