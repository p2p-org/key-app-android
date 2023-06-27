package org.p2p.token.service

import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RPC_JSON_QUALIFIER
import org.p2p.token.service.api.TokenServiceApiRepository
import org.p2p.token.service.repository.TokenServiceLocalRepository
import org.p2p.token.service.api.TokenServiceApiRepositoryImpl
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.interactor.TokenServiceInteractor
import org.p2p.token.service.repository.TokenServiceInMemoryRepository
import org.p2p.token.service.repository.TokenServiceRemoteRepository
import org.p2p.token.service.repository.TokenServiceRepository

object TokenPriceModule : InjectionModule {
    private const val TOKEN_SERVICE_RETROFIT_QUALIFIER = "TOKEN_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        single<TokenServiceApiRepository> {
            TokenServiceApiRepositoryImpl(
                api = get<Retrofit>(named(TOKEN_SERVICE_RETROFIT_QUALIFIER)).create(),
                gson = get(named(RPC_JSON_QUALIFIER)),
                urlProvider = get()
            )
        }

        single(named(TOKEN_SERVICE_RETROFIT_QUALIFIER)) {
            val url = get<NetworkServicesUrlProvider>()
            getRetrofit(
                baseUrl = url.loadTokenServiceEnvironment().baseServiceUrl,
                tag = "RpcBridge",
                interceptor = null
            )
        }

        single<TokenServiceLocalRepository> { TokenServiceInMemoryRepository() }
        factory<TokenServiceRepository> { TokenServiceRemoteRepository(get(), get()) }
        factory { TokenServiceMapper() }
        factory { TokenServiceInteractor(get(), get()) }
    }
}
