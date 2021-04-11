package com.p2p.wowlet.dashboard

import com.p2p.wowlet.auth.interactor.EnterPinCodeInteractor
import com.p2p.wowlet.common.di.InjectionModule
import com.p2p.wowlet.dashboard.repository.DashboardRepository
import com.p2p.wowlet.dashboard.repository.DashboardRepositoryImpl
import com.p2p.wowlet.dashboard.repository.DetailActivityRepository
import com.p2p.wowlet.dashboard.repository.DetailActivityRepositoryImpl
import com.p2p.wowlet.dashboard.repository.LocalDatabaseRepository
import com.p2p.wowlet.dashboard.repository.LocalDatabaseRepositoryImpl
import com.p2p.wowlet.dashboard.repository.WowletApiCallRepository
import com.p2p.wowlet.dashboard.repository.WowletApiCallRepositoryImpl
import com.p2p.wowlet.dashboard.ui.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.dashboard.ui.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wowlet.dashboard.ui.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wowlet.dashboard.ui.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wowlet.dashboard.ui.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.dashboard.ui.dialog.sendcoins.viewmodel.WalletAddressViewModel
import com.p2p.wowlet.dashboard.interactor.DashboardInteractor
import com.p2p.wowlet.backupwallat.interactor.ManualSecretKeyInteractor
import com.p2p.wowlet.dashboard.interactor.NotificationInteractor
import com.p2p.wowlet.dashboard.interactor.QrScannerInteractor
import com.p2p.wowlet.backupwallat.interactor.SecretKeyInteractor
import com.p2p.wowlet.dashboard.interactor.SendCoinInteractor
import com.p2p.wowlet.dashboard.interactor.SwapInteractor
import com.p2p.wowlet.qrscanner.viewmodel.QrScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DashboardModule : InjectionModule {

    // todo: workaround, split to modules, make dashboard simpler
    override fun create() = module {
        single<WowletApiCallRepository> { WowletApiCallRepositoryImpl(get(), get()) }
        single<DashboardRepository> { DashboardRepositoryImpl(get()) }
        single<DetailActivityRepository> { DetailActivityRepositoryImpl(get()) }
        single<LocalDatabaseRepository> { LocalDatabaseRepositoryImpl(get()) }
        viewModel { DashboardViewModel(get(), get(), get()) }
        viewModel { QrScannerViewModel(get(), get()) }
        viewModel { DetailWalletViewModel(get()) }
        viewModel { SendCoinsViewModel(get(), get()) }
        viewModel { SwapViewModel(get()) }

        viewModel { NetworkViewModel(get()) }
        viewModel { ProfileViewModel(get(), get()) }
        viewModel { WalletAddressViewModel() }

        factory { QrScannerInteractor(get()) }
        factory { SwapInteractor() }

        factory { SecretKeyInteractor(get(), get()) }
        factory { ManualSecretKeyInteractor() }
        factory { EnterPinCodeInteractor(get()) }
        single { NotificationInteractor(get()) }
        single { SendCoinInteractor(get(), get()) }
        single { DashboardInteractor(get(), get(), get(), get()) }

    }
}