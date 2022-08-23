package org.p2p.wallet.restore

import org.koin.core.module.dsl.factoryOf
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
        factoryOf(::SecretKeyInteractor)
        factory<SecretKeyContract.Presenter> {
            SecretKeyPresenter(
                secretKeyInteractor = get(),
            )
        }
        factory<DerivableAccountsContract.Presenter> { (secretKeys: List<SecretKey>) ->
            DerivableAccountsPresenter(
                secretKeys = secretKeys,
                secretKeyInteractor = get(),
                usernameInteractor = get(),
                analytics = get()
            )
        }
    }
}
