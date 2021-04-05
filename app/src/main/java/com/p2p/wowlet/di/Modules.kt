package com.p2p.wowlet.di


import com.p2p.wowlet.fragment.backupwallat.completebackupwallet.viewmodel.CompleteBackupWalletViewModel
import com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wowlet.fragment.createwallet.viewmodel.CreateWalletViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wowlet.fragment.fingetprint.viewmodel.FingerPrintViewModel
import com.p2p.wowlet.fragment.investments.viewmodel.InvestmentsViewModel
import com.p2p.wowlet.fragment.notification.viewmodel.NotificationViewModel
import com.p2p.wowlet.fragment.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.p2p.wowlet.fragment.regfinish.viewmodel.RegFinishViewModel
import com.p2p.wowlet.fragment.regwallet.viewmodel.RegWalletViewModel
import com.p2p.wowlet.fragment.search.viewmodel.SearchViewModel
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.dialog.sendcoins.viewmodel.WalletAddressViewModel
import com.p2p.wowlet.fragment.splashscreen.viewmodel.SplashScreenViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.swap.viewmodel.SwapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModule = module {
    viewModel { SecretKeyViewModel(get()) }
    viewModel { RegFinishViewModel(get()) }
    viewModel { SplashScreenViewModel(get()) }
    viewModel { FingerPrintViewModel(get()) }
    viewModel { NotificationViewModel(get()) }
    viewModel { CompleteBackupWalletViewModel(get()) }
    viewModel { RegWalletViewModel() }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { SearchViewModel() }
    viewModel { InvestmentsViewModel() }
    viewModel { QrScannerViewModel(get(), get()) }
    viewModel { DetailWalletViewModel(get()) }
    viewModel { com.p2p.wowlet.fragment.detailwallet.viewmodel.DetailWalletViewModel(get()) }
    viewModel { DetailSavingViewModel() }
    viewModel { RecoveryPhraseViewModel(get()) }
    viewModel { SendCoinsViewModel(get(), get()) }
    viewModel { SwapViewModel(get()) }
    viewModel { CreateWalletViewModel(get()) }
    viewModel { PinCodeViewModel(get(), get(), get()) }
    viewModel { ManualSecretKeyViewModel(get()) }
    viewModel { NetworkViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { WalletAddressViewModel() }
}
