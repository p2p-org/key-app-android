package org.p2p.wallet.sell

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.lock.SellLockContract
import org.p2p.wallet.sell.ui.lock.SellLockPresenter
import org.p2p.wallet.sell.ui.payload.SellPayloadContract
import org.p2p.wallet.sell.ui.payload.SellPayloadPresenter

object SellModule : InjectionModule {
    override fun create() = module {
        factoryOf(::SellInteractor)
        factory { SellPayloadPresenter() } bind SellPayloadContract.Presenter::class
        factory { SellLockPresenter() } bind SellLockContract.Presenter::class
    }
}
