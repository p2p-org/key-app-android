package org.p2p.wallet.moonpay.ui

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.core.common.di.InjectionModule
import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.interactor.BuyInteractor
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.model.PaymentMethod
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
        factory<NewBuyContract.Presenter> { (
                                                token: Token,
                                                fiatToken: String?,
                                                fiatAmount: String?,
                                                preselectedMethodType: PaymentMethod.MethodType?
                                            ) ->
            NewBuyPresenter(
                tokenToBuy = token,
                fiatToken = fiatToken,
                fiatAmount = fiatAmount,
                preselectedMethodType = preselectedMethodType,
                buyAnalytics = get(),
                userInteractor = get(),
                paymentMethodsInteractor = get(),
                resources = get(),
                buyInteractor = get(),
                analyticsInteractor = get()
            )
        }
    }
}
