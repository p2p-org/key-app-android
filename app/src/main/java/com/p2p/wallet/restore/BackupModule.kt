package com.p2p.wallet.restore

import com.p2p.wallet.common.di.InjectionModule
import com.p2p.wallet.restore.interactor.SecretKeyInteractor
import com.p2p.wallet.restore.ui.keys.SecretKeyContract
import com.p2p.wallet.restore.ui.keys.SecretKeyPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object BackupModule : InjectionModule {

    override fun create() = module {
        factory { SecretKeyInteractor(get()) }
        factory { SecretKeyPresenter(get()) } bind SecretKeyContract.Presenter::class
    }
}