package org.p2p.wallet.history

import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.repository.HistoryRemoteRepository
import org.p2p.wallet.history.repository.HistoryRepository
import org.p2p.wallet.history.ui.main.HistoryContract
import org.p2p.wallet.history.ui.main.HistoryPresenter
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

object HistoryModule : InjectionModule {

    override fun create(): Module = module {

        factory { HistoryRemoteRepository(get()) } bind HistoryRepository::class
        factory { HistoryInteractor(get(), get(), get(), get()) }
        factory { HistoryPresenter(get(), get()) } bind HistoryContract.Presenter::class
    }
}