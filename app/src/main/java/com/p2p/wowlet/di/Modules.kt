package com.p2p.wowlet.di

import com.p2p.wowlet.auth.ui.createwallet.viewmodel.CreateWalletViewModel
import com.p2p.wowlet.auth.ui.fingerprint.viewmodel.FingerPrintViewModel
import com.p2p.wowlet.auth.ui.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.backupwallat.completebackupwallet.viewmodel.CompleteBackupWalletViewModel
import com.p2p.wowlet.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wowlet.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wowlet.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.dashboard.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wowlet.dashboard.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wowlet.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wowlet.dashboard.dialog.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.dialog.sendcoins.viewmodel.WalletAddressViewModel
import com.p2p.wowlet.investments.viewmodel.InvestmentsViewModel
import com.p2p.wowlet.notification.viewmodel.NotificationViewModel
import com.p2p.wowlet.qrscanner.viewmodel.QrScannerViewModel
import com.p2p.wowlet.search.viewmodel.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModule = module {
    viewModel { SecretKeyViewModel(get()) }
    viewModel { FingerPrintViewModel(get()) }
    viewModel { NotificationViewModel(get()) }
    viewModel { CompleteBackupWalletViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get()) }
    viewModel { SearchViewModel() }
    viewModel { InvestmentsViewModel() }
    viewModel { QrScannerViewModel(get(), get()) }
    viewModel { DetailWalletViewModel(get()) }
    viewModel { com.p2p.wowlet.detailwallet.viewmodel.DetailWalletViewModel(get()) }
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