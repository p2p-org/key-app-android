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
import org.p2p.token.service.api.coingecko.CoinGeckoDataSource
import org.p2p.token.service.api.coingecko.CoinGeckoTokenPriceRepository
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.api.jupiter.JupiterPricesDataSource
import org.p2p.token.service.api.tokenservice.TokenServiceDataSource
import org.p2p.token.service.api.tokenservice.TokenServiceRemoteDataSource
import org.p2p.token.service.converter.TokenServiceAmountsConverter
import org.p2p.token.service.converter.TokenServiceAmountsRemoteConverter
import org.p2p.token.service.database.TokenServiceDatabaseModule
import org.p2p.token.service.database.mapper.TokenServiceDatabaseMapper
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.token.service.repository.TokenServiceRepositoryImpl
import org.p2p.token.service.repository.mapper.TokenServiceMapper
import org.p2p.token.service.repository.metadata.TokenMetadataInMemoryRepository
import org.p2p.token.service.repository.metadata.TokenMetadataLocalRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRemoteRepository
import org.p2p.token.service.repository.metadata.TokenMetadataRepository
import org.p2p.token.service.repository.price.JupiterTokenPriceRepository
import org.p2p.token.service.repository.price.TokenPriceDatabaseRepository
import org.p2p.token.service.repository.price.TokenPriceLocalRepository
import org.p2p.token.service.repository.price.TokenPriceRemoteRepository
import org.p2p.token.service.repository.price.TokenPriceRepository

object TokenServiceModule : InjectionModule {
    private const val TOKEN_SERVICE_RETROFIT_QUALIFIER = "TOKEN_SERVICE_RETROFIT_QUALIFIER"

    override fun create() = module {
        includes(TokenServiceDatabaseModule.create())
        single<TokenServiceDataSource> {
            TokenServiceRemoteDataSource(
                api = get<Retrofit>(named(TOKEN_SERVICE_RETROFIT_QUALIFIER)).create(),
                gson = get(named(RPC_JSON_QUALIFIER)),
                urlProvider = get()
            )
        }

        single<JupiterPricesDataSource> {
            getRetrofit(baseUrl = "https://price.jup.ag/", interceptor = null).create()
        }

        single<CoinGeckoDataSource> {
            getRetrofit(
                baseUrl = "https://api.coingecko.com/api/v3/",
                tag = "CoinGeckoApi",
                interceptor = null,
            ).create()
        }

        single(named(TOKEN_SERVICE_RETROFIT_QUALIFIER)) {
            val url = get<NetworkServicesUrlProvider>()
            getRetrofit(
                baseUrl = url.loadTokenServiceEnvironment().baseServiceUrl,
                tag = "TokenService",
                interceptor = null
            )
        }

        singleOf(::TokenMetadataInMemoryRepository) bind TokenMetadataLocalRepository::class
        singleOf(::TokenPriceDatabaseRepository) bind TokenPriceLocalRepository::class
        factoryOf(::TokenPriceRemoteRepository) bind TokenPriceRepository::class
        factoryOf(::JupiterTokenPriceRepository)
        // caches data across app
        singleOf(::CoinGeckoTokenPriceRepository)
        factoryOf(::TokenServiceAmountsRemoteConverter) bind TokenServiceAmountsConverter::class

        factoryOf(::TokenServiceMapper)
        singleOf(::TokenMetadataRemoteRepository) bind TokenMetadataRepository::class
        factory<TokenServiceRepository> {
            TokenServiceRepositoryImpl(
                priceRemoteRepository = get(),
                priceLocalRepository = get(),
                metadataLocalRepository = get(),
                metadataRemoteRepository = get(),
                jupiterPriceRepository = get(),
                coinGeckoTokenPriceRepository = get()
            )
        }
        singleOf(::TokenServiceEventPublisher)
        singleOf(::TokenServiceEventManager)
        singleOf(::TokenServiceDatabaseMapper)
    }
}
