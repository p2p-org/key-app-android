package org.p2p.wallet.moonpay

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.wallet.R
import org.p2p.wallet.common.ResourcesProvider
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.interactor.BuyInteractor
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.ui.BuySolanaContract
import org.p2p.wallet.moonpay.ui.BuySolanaPresenter
import org.p2p.wallet.moonpay.ui.new.NewBuyContract
import org.p2p.wallet.moonpay.ui.new.NewBuyPresenter
import org.p2p.wallet.receive.solana.ReceiveSolanaContract
import org.p2p.wallet.receive.solana.ReceiveSolanaPresenter

object BuyModule : InjectionModule {

    override fun create() = module {
        initDomainLayer()
        initPresentationLayer()
    }

    private fun Module.initDomainLayer() {
        factoryOf(::PaymentMethodsInteractor)
        factoryOf(::BuyInteractor)
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
                moonpayBuyRepository = get(),
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
                buyInteractor = get(),
                analyticsInteractor = get()
            )
        }
    }
}
