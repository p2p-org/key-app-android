package org.p2p.wallet.sell

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorage
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsContract
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsPresenter
import org.p2p.wallet.sell.interactor.SellInteractor
import org.p2p.wallet.sell.ui.information.SellInformationContract
import org.p2p.wallet.sell.ui.information.SellInformationPresenter
import org.p2p.wallet.sell.ui.lock.SellLockedContract
import org.p2p.wallet.sell.ui.lock.SellLockedPresenter
import org.p2p.wallet.sell.ui.payload.SellPayloadContract
import org.p2p.wallet.sell.ui.payload.SellPayloadPresenter

object SellModule : InjectionModule {
    override fun create() = module {
        factoryOf(::HiddenSellTransactionsStorage) bind HiddenSellTransactionsStorageContract::class

        factoryOf(::SellInteractor)
        factoryOf(::SellPayloadPresenter) bind SellPayloadContract.Presenter::class
        factoryOf(::SellLockedPresenter) bind SellLockedContract.Presenter::class
        factoryOf(::SellTransactionDetailsPresenter) bind SellTransactionDetailsContract.Presenter::class
        factoryOf(::SellInformationPresenter) bind SellInformationContract.Presenter::class
    }
}
