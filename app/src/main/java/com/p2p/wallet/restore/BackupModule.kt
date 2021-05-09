package com.p2p.wallet.restore

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.restore.ui.secretkeys.viewmodel.SecretKeyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object BackupModule : InjectionModule {

    override fun create() = module {
        viewModel { SecretKeyViewModel(get()) }
    }
}