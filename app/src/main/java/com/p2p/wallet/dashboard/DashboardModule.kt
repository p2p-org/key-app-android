package com.p2p.wallet.dashboard

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.dashboard.interactor.DashboardInteractor
import com.p2p.wallet.dashboard.interactor.SwapInteractor
import com.p2p.wallet.dashboard.repository.WowletApiCallRepository
import com.p2p.wallet.dashboard.repository.WowletApiCallRepositoryImpl
import com.p2p.wallet.dashboard.ui.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DashboardModule : InjectionModule {

    // todo: workaround, split to modules, make dashboard simpler
    override fun create() = module {
        single<WowletApiCallRepository> { WowletApiCallRepositoryImpl(get()) }
        viewModel { SwapViewModel(get()) }

        factory { SwapInteractor() }

        factory { SecretKeyInteractor(get()) }
        single { DashboardInteractor(get()) }
    }
}