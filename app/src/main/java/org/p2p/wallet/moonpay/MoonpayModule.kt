package org.p2p.wallet.moonpay

import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.HomeModule
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.api.MoonpayUrlBuilder
import org.p2p.wallet.moonpay.repository.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.MoonpayBuyRemoteRepository
import org.p2p.wallet.moonpay.repository.MoonpayBuyRepository
import org.p2p.wallet.moonpay.repository.NewMoonpayBuyRemoteRepository
import org.p2p.wallet.moonpay.repository.NewMoonpayBuyRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRemoteRepository
import org.p2p.wallet.moonpay.repository.sell.MoonpaySellRepository
import retrofit2.Retrofit

object MoonpayModule : InjectionModule {
    override fun create() = module {
        factoryOf(::MoonpayApiMapper)
        factory<MoonpayBuyRepository> {
            val api = get<Retrofit>(named(HomeModule.MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            MoonpayBuyRemoteRepository(
                api = api,
                moonpayApiKey = apiKey,
                mapper = get()
            )
        }
        factory<NewMoonpayBuyRepository> {
            val api = get<Retrofit>(named(HomeModule.MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            NewMoonpayBuyRemoteRepository(api, apiKey, get())
        }

        single<MoonpaySellRepository> {
            val api = get<Retrofit>(named(HomeModule.MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            MoonpaySellRemoteRepository(
                moonpayApi = api,
                sellFeatureToggle = get(),
                moonpayApiKey = apiKey,
                dispatchers = get()
            )
        }

        factory { MoonpayUrlBuilder }
    }
}
