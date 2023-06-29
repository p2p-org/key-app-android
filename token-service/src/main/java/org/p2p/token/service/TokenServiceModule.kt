package org.p2p.token.service

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.core.network.environment.NetworkServicesUrlProvider
import org.p2p.core.rpc.RPC_JSON_QUALIFIER
import org.p2p.token.service.api.TokenServiceRepository
import org.p2p.token.service.repository.price.TokenPriceLocalRepository
import org.p2p.token.service.api.TokenServiceRemoteRepository
import org.p2p.token.service.api.mapper.TokenServiceMapper
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.repository.TokenServiceRepositoryImpl
import org.p2p.token.service.repository.metadata.TokenMetadataInMemoryRepository
import org.p2p.token.service.repository.metadata.TokenMetadataLocalRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRemoteRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.token.service.repository.price.TokenPriceInMemoryRepository
import org.p2p.token.service.repository.price.TokenPriceRemoteRepository
import org.p2p.token.service.repository.price.TokenPriceRepository

object TokenServiceModule : InjectionModule {
    private const val TOKEN_SERVICE_RETROFIT_QUALIFIER = "TOKEN_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        single<TokenServiceRepository> {
            TokenServiceRemoteRepository(
                api = get<Retrofit>(named(TOKEN_SERVICE_RETROFIT_QUALIFIER)).create(),
                gson = get(named(RPC_JSON_QUALIFIER)),
                urlProvider = get()
            )
        }

        single(named(TOKEN_SERVICE_RETROFIT_QUALIFIER)) {
            val url = get<NetworkServicesUrlProvider>()
            getRetrofit(
                baseUrl = url.loadTokenServiceEnvironment().baseServiceUrl,
                tag = "TokenService",
                interceptor = null
            )
        }

        single<TokenMetadataLocalRepository> { TokenMetadataInMemoryRepository() }
        factory<TokenMetadataRepository> { TokenMetadataRemoteRepository(get(), get()) }
        single<TokenPriceLocalRepository> { TokenPriceInMemoryRepository() }
        factory<TokenPriceRepository> { TokenPriceRemoteRepository(get(), get()) }

        factoryOf(::TokenServiceMapper)
        factoryOf(::TokenServiceRepositoryImpl) bind org.p2p.token.service.repository.TokenServiceRepository::class
        factoryOf(::TokenServiceRepositoryImpl)
        factoryOf(::TokenServiceEventPublisher)
        singleOf(::TokenServiceEventManager)
    }
}
