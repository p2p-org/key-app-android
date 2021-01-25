package com.p2p.wowlet.di

import com.p2p.wowlet.fragment.backupwallat.completebackupwallet.viewmodel.CompleteBackupWalletViewModel
import com.p2p.wowlet.fragment.backupwallat.manualsecretkeys.viewmodel.ManualSecretKeyViewModel
import com.p2p.wowlet.fragment.backupwallat.recoverywallat.viewmodel.RecoveryWalletViewModel
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wowlet.fragment.blockchainexplorer.viewmodel.BlockChainExplorerViewModel
import com.p2p.wowlet.fragment.contacts.viewmodel.ContactsViewModel
import com.p2p.wowlet.fragment.createwallet.viewmodel.CreateWalletViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.networks.viewmodel.NetworkViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.profile.viewmodel.ProfileViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.detailwallet.viewmodel.DetailWalletViewModel
import com.p2p.wowlet.fragment.fingetprint.viewmodel.FingerPrintViewModel
import com.p2p.wowlet.fragment.investments.viewmodel.InvestmentsViewModel
import com.p2p.wowlet.fragment.notification.viewmodel.NotificationViewModel
import com.p2p.wowlet.fragment.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.p2p.wowlet.fragment.regfinish.viewmodel.RegFinishViewModel
import com.p2p.wowlet.fragment.reglogin.viewmodel.RegLoginViewModel
import com.p2p.wowlet.fragment.regwallet.viewmodel.RegWalletViewModel
import com.p2p.wowlet.fragment.search.viewmodel.SearchViewModel
import com.p2p.wowlet.dialog.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.fragment.splashscreen.viewmodel.SplashScreenViewModel
import com.p2p.wowlet.fragment.swap.viewmodel.SwapViewModel
import com.p2p.wowlet.fragment.termandcondition.viewmodel.TermAndConditionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModule = module {
    viewModel { SecretKeyViewModel(get()) }
    viewModel { RegFinishViewModel(get()) }
    viewModel { SplashScreenViewModel(get()) }
    viewModel { FingerPrintViewModel(get()) }
    viewModel { NotificationViewModel(get()) }
    viewModel { RegLoginViewModel(get()) }
    viewModel { RecoveryWalletViewModel() }
    viewModel { CompleteBackupWalletViewModel(get()) }
    viewModel { RegWalletViewModel() }
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { SearchViewModel() }
    viewModel { InvestmentsViewModel() }
    viewModel { ContactsViewModel() }
    viewModel { QrScannerViewModel(get()) }
    viewModel { DetailWalletViewModel(get()) }
    viewModel { DetailSavingViewModel() }
    viewModel { RecoveryPhraseViewModel(get()) }
    viewModel { SendCoinsViewModel(get(), get()) }
    viewModel { SwapViewModel() }
    viewModel { CreateWalletViewModel(get()) }
    viewModel { TermAndConditionViewModel() }
    viewModel { PinCodeViewModel(get(), get(), get()) }
    viewModel { BlockChainExplorerViewModel() }
    viewModel { ManualSecretKeyViewModel(get()) }
    viewModel { NetworkViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
}
