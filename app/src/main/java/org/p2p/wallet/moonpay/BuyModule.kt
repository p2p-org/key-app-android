package org.p2p.wallet.moonpay

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.HomeModule
import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.api.MoonpayApi
import org.p2p.wallet.moonpay.interactor.MoonpayBuyInteractor
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.repository.MoonpayApiMapper
import org.p2p.wallet.moonpay.repository.MoonpayRemoteRepository
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.moonpay.repository.NewMoonpayRemoteRepository
import org.p2p.wallet.moonpay.repository.NewMoonpayRepository
import org.p2p.wallet.moonpay.ui.BuySolanaContract
import org.p2p.wallet.moonpay.ui.BuySolanaPresenter
import org.p2p.wallet.moonpay.ui.new.NewBuyContract
import org.p2p.wallet.moonpay.ui.new.NewBuyPresenter
import org.p2p.wallet.receive.solana.ReceiveSolanaContract
import org.p2p.wallet.receive.solana.ReceiveSolanaPresenter
import retrofit2.Retrofit

object BuyModule : InjectionModule {

    override fun create() = module {
        initDataLayer()
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDataLayer() {
        factory { MoonpayApiMapper() }
        factory<MoonpayRepository>() {
            val api = get<Retrofit>(named(HomeModule.MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            MoonpayRemoteRepository(api, apiKey, get())
        }
        factory<NewMoonpayRepository>() {
            val api = get<Retrofit>(named(HomeModule.MOONPAY_QUALIFIER)).create(MoonpayApi::class.java)
            val apiKey = BuildConfig.moonpayKey
            NewMoonpayRemoteRepository(api, apiKey, get())
        }
    }

    private fun Module.initDomainLayer() {
        factoryOf(::PaymentMethodsInteractor)
        factoryOf(::MoonpayBuyInteractor)
    }

    private fun Module.initPresentationLayer() {
        factory<ReceiveSolanaContract.Presenter> { (token: Token.Active?) ->
            ReceiveSolanaPresenter(
                defaultToken = token,
                userInteractor = get(),
                qrCodeInteractor = get(),
                usernameInteractor = get(),
                tokenKeyProvider = get(),
                receiveAnalytics = get(),
                context = get()
            )
        }
        factory<BuySolanaContract.Presenter> { (token: Token) ->
            BuySolanaPresenter(
                tokenToBuy = token,
                moonpayRepository = get(),
                minBuyErrorFormat = get<ResourcesProvider>().getString(R.string.buy_min_error_format),
                maxBuyErrorFormat = get<ResourcesProvider>().getString(R.string.buy_max_error_format),
                buyAnalytics = get(),
                analyticsInteractor = get()
            )
        }
        factory<NewBuyContract.Presenter> { (token: Token) ->
            NewBuyPresenter(
                tokenToBuy = token,
                buyAnalytics = get(),
                userInteractor = get(),
                paymentMethodsInteractor = get(),
                resourcesProvider = get(),
                bankTransferFeatureToggle = get(),
                moonpayBuyInteractor = get(),
                analyticsInteractor = get()
            )
        }
    }
}
