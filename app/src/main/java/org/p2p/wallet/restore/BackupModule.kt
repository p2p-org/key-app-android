package org.p2p.wallet.restore

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsContract
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsPresenter
import org.p2p.wallet.restore.ui.keys.SecretKeyContract
import org.p2p.wallet.restore.ui.keys.SecretKeyPresenter

object BackupModule : InjectionModule {

    override fun create() = module {
        factory {
            SecretKeyInteractor(get(), get(), get(), get(), get(), get(), get())
        }
        factory<SecretKeyContract.Presenter> {
            SecretKeyPresenter(androidContext().resources, get(), get())
        }
        factory<DerivableAccountsContract.Presenter> { (secretKeys: List<SecretKey>) ->
            DerivableAccountsPresenter(secretKeys, get(), get(), get())
        }
    }
}
