package org.p2p.wallet.sell

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.lock.SellLockedContract
import org.p2p.wallet.sell.ui.lock.SellLockedPresenter
import org.p2p.wallet.sell.ui.payload.SellPayloadContract
import org.p2p.wallet.sell.ui.payload.SellPayloadPresenter

object SellModule : InjectionModule {
    override fun create() = module {
        factoryOf(::SellInteractor)
        factory {
            SellPayloadPresenter(
                get(),
                get(),
                get(),
                get(),
            )
        } bind SellPayloadContract.Presenter::class
        factoryOf(::SellLockedPresenter) bind SellLockedContract.Presenter::class

//        factoryOf(::MoonpaySellWidgetPresenter) bind MoonpaySellWidgetContract.Presenter::class
    }
}
