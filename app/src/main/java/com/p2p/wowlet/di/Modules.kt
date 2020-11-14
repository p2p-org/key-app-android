package com.p2p.wowlet.di

import com.p2p.wowlet.fragment.backupwallat.completebackupwallet.viewmodel.BackupWalletViewModel
import com.p2p.wowlet.fragment.backupwallat.recoverywallat.viewmodel.RecoveryWalletViewModel
import com.p2p.wowlet.fragment.backupwallat.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wowlet.fragment.dashboard.viewmodel.DashboardViewModel
import com.p2p.wowlet.fragment.detailsaving.viewmodel.DetailSavingViewModel
import com.p2p.wowlet.fragment.faceid.viewmodel.FaceIdViewModel
import com.p2p.wowlet.fragment.investments.viewmodel.InvestmentsViewModel
import com.p2p.wowlet.fragment.notification.viewmodel.NotificationViewModel
import com.p2p.wowlet.fragment.pincode.viewmodel.PinCodeViewModel
import com.p2p.wowlet.fragment.contacts.viewmodel.ContactsViewModel
import com.p2p.wowlet.fragment.dashboard.dialog.recoveryphrase.viewmodel.RecoveryPhraseViewModel
import com.p2p.wowlet.fragment.qrscanner.viewmodel.QrScannerViewModel
import com.p2p.wowlet.fragment.receive.viewmodel.ReceiveViewModel
import com.p2p.wowlet.fragment.regfinish.viewmodel.RegFinishViewModel
import com.p2p.wowlet.fragment.reglogin.viewmodel.RegLoginViewModel
import com.p2p.wowlet.fragment.regwallet.viewmodel.RegWalletViewModel
import com.p2p.wowlet.fragment.search.viewmodel.SearchViewModel
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import com.p2p.wowlet.fragment.splashscreen.viewmodel.SplashScreenViewModel
import com.p2p.wowlet.fragment.swap.viewmodel.SwapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModule = module {
    viewModel { SecretKeyViewModel(get()) }
    viewModel { RegFinishViewModel() }
    viewModel { SplashScreenViewModel() }
    viewModel { FaceIdViewModel() }
    viewModel { NotificationViewModel(get()) }
    viewModel { PinCodeViewModel(get()) }
    viewModel { RegLoginViewModel(get()) }
    viewModel { RecoveryWalletViewModel() }
    viewModel { BackupWalletViewModel() }
    viewModel { RegWalletViewModel() }
    viewModel { DashboardViewModel(get()) }
    viewModel { SearchViewModel() }
    viewModel { InvestmentsViewModel() }
    viewModel { ContactsViewModel() }
    viewModel { QrScannerViewModel() }
    viewModel { ReceiveViewModel() }
    viewModel { DetailSavingViewModel() }
    viewModel { RecoveryPhraseViewModel() }
    viewModel { SendCoinsViewModel(get()) }
    viewModel { SwapViewModel() }
}
