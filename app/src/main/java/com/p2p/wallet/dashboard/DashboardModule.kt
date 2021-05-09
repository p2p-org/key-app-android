package com.p2p.wallet.dashboard

import com.p2p.wallet.auth.interactor.EnterPinCodeInteractor
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.dashboard.interactor.DashboardInteractor
import com.p2p.wallet.dashboard.interactor.NotificationInteractor
import com.p2p.wallet.dashboard.interactor.QrScannerInteractor
import com.p2p.wallet.dashboard.interactor.SendCoinInteractor
import com.p2p.wallet.dashboard.interactor.SwapInteractor
import com.p2p.wallet.dashboard.repository.DashboardRepository
import com.p2p.wallet.dashboard.repository.DashboardRepositoryImpl
import com.p2p.wallet.dashboard.repository.DetailActivityRepository
import com.p2p.wallet.dashboard.repository.DetailActivityRepositoryImpl
import com.p2p.wallet.dashboard.repository.LocalDatabaseRepository
import com.p2p.wallet.dashboard.repository.LocalDatabaseRepositoryImpl
import com.p2p.wallet.dashboard.repository.WowletApiCallRepository
import com.p2p.wallet.dashboard.repository.WowletApiCallRepositoryImpl
import com.p2p.wallet.dashboard.ui.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wallet.dashboard.ui.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wallet.dashboard.ui.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wallet.dashboard.ui.dialog.sendcoins.viewmodel.WalletAddressViewModel
import com.p2p.wallet.dashboard.ui.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wallet.dashboard.ui.viewmodel.DashboardViewModel
import com.p2p.wallet.qrscanner.viewmodel.QrScannerViewModel
import com.p2p.wallet.restore.interactor.ManualSecretKeyInteractor
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object DashboardModule : InjectionModule {

    // todo: workaround, split to modules, make dashboard simpler
    override fun create() = module {
        single<WowletApiCallRepository> { WowletApiCallRepositoryImpl(get(), get()) }
        single<DashboardRepository> { DashboardRepositoryImpl(get()) }
        single<DetailActivityRepository> { DetailActivityRepositoryImpl(get()) }
        single<LocalDatabaseRepository> { LocalDatabaseRepositoryImpl(get()) }
        viewModel { DashboardViewModel(get(), get()) }
        viewModel { QrScannerViewModel(get(), get()) }
        viewModel { DetailWalletViewModel(get()) }
        viewModel { SendCoinsViewModel(get(), get()) }
        viewModel { SwapViewModel(get()) }

        viewModel { NetworkViewModel(get()) }
        viewModel { ProfileViewModel(get()) }
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