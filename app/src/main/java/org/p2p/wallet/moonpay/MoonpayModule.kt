package org.p2p.wallet.moonpay

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
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
        factory<MoonpayApi> {
            val retrofit = get<Retrofit>(named(HomeModule.MOONPAY_QUALIFIER))
            retrofit.create(MoonpayApi::class.java)
        }
        factoryOf(::MoonpayBuyRemoteRepository) bind MoonpayBuyRepository::class
        factoryOf(::NewMoonpayBuyRemoteRepository) bind NewMoonpayBuyRepository::class
        singleOf(::MoonpaySellRemoteRepository) bind MoonpaySellRepository::class
        factory { MoonpayUrlBuilder }
    }
}
