package org.p2p.wallet.striga.exchange

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.create
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.network.NetworkCoreModule.getRetrofit
import org.p2p.wallet.R
import org.p2p.wallet.infrastructure.network.interceptor.StrigaProxyApiInterceptor
import org.p2p.wallet.striga.exchange.api.StrigaExchangeApi
import org.p2p.wallet.striga.exchange.repository.StrigaExchangeRepository
import org.p2p.wallet.striga.exchange.repository.impl.StrigaExchangeRemoteRepository
import org.p2p.wallet.striga.exchange.repository.mapper.StrigaExchangeRepositoryMapper

object StrigaExchangeModule : InjectionModule {
    override fun create(): Module = module {
        single<StrigaExchangeApi> {
            val url = androidContext().getString(R.string.strigaProxyServiceBaseUrl)
            getRetrofit(
                baseUrl = url,
                tag = "StrigaProxyApi",
                interceptor = new(::StrigaProxyApiInterceptor)
            ).create()
        }

        factoryOf(::StrigaExchangeRemoteRepository) bind StrigaExchangeRepository::class
        factoryOf(::StrigaExchangeRepositoryMapper)
    }
}
