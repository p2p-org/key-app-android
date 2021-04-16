package com.p2p.wallet.restore

import com.p2p.wallet.restore.completebackupwallet.viewmodel.CompleteBackupWalletViewModel
import com.p2p.wallet.restore.secretkeys.viewmodel.SecretKeyViewModel
import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.restore.interactor.CompleteBackupWalletInteractor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object BackupModule : InjectionModule {

    override fun create() = module {
        viewModel { SecretKeyViewModel(get()) }
        viewModel { CompleteBackupWalletViewModel(get()) }
        single { CompleteBackupWalletInteractor(get()) }
    }
}