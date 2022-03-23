package org.p2p.wallet.restore

import org.koin.android.ext.koin.androidContext
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsContract
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsPresenter
import org.p2p.wallet.restore.ui.keys.SecretKeyContract
import org.p2p.wallet.restore.ui.keys.SecretKeyPresenter
import org.koin.dsl.bind
import org.koin.dsl.module

object BackupModule : InjectionModule {

    override fun create() = module {
        factory { SecretKeyInteractor(get(), get(), get(), get(), get(), get(), get()) }
        factory { SecretKeyPresenter(androidContext().resources, get(), get()) } bind SecretKeyContract.Presenter::class
        factory { (secretKeys: List<SecretKey>) ->
            DerivableAccountsPresenter(secretKeys, get(), get(), get())
        } bind DerivableAccountsContract.Presenter::class
    }
}
