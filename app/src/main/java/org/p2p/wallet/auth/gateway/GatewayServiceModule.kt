package org.p2p.wallet.auth.gateway

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.repository.GatewayServiceErrorMapper
import org.p2p.wallet.auth.gateway.repository.GatewayServiceCreateWalletMapper
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRemoteRepository
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRestoreWalletMapper
import org.p2p.wallet.auth.gateway.repository.GatewayServiceSignatureFieldGenerator
import org.p2p.wallet.common.di.InjectionModule
import retrofit2.Retrofit
import retrofit2.create

object GatewayServiceModule : InjectionModule {

    const val FACADE_SERVICE_RETROFIT_QUALIFIER = "FACADE_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        single<GatewayServiceApi> { get<Retrofit>(named(FACADE_SERVICE_RETROFIT_QUALIFIER)).create() }
        factoryOf(::GatewayServiceErrorMapper)
        singleOf(::GatewayServiceCreateWalletMapper)
        singleOf(::GatewayServiceRestoreWalletMapper)
        singleOf(::GatewayServiceSignatureFieldGenerator)
        singleOf(::GatewayServiceRemoteRepository) bind GatewayServiceRepository::class
    }
}
