package org.p2p.wallet.history

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.repository.HistoryRemoteRepository
import org.p2p.wallet.history.repository.HistoryRepository
import org.p2p.wallet.history.ui.history.HistoryContract
import org.p2p.wallet.history.ui.history.HistoryPresenter
import org.p2p.wallet.history.ui.info.TokenInfoContract
import org.p2p.wallet.history.ui.info.TokenInfoPresenter
import org.p2p.wallet.main.model.Token

object HistoryModule : InjectionModule {

    override fun create(): Module = module {

        factory { HistoryRemoteRepository(get()) } bind HistoryRepository::class
        factory { HistoryInteractor(get(), get(), get(), get()) }
        factory { (token: Token.Active) -> HistoryPresenter(token, get()) } bind HistoryContract.Presenter::class
        factory { TokenInfoPresenter(get()) } bind TokenInfoContract.Presenter::class
    }
}