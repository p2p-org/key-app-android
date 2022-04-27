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
            SecretKeyInteractor(
                authRepository = get(),
                userLocalRepository = get(),
                rpcRepository = get(),
                tokenProvider = get(),
                sharedPreferences = get(),
                usernameInteractor = get(),
                adminAnalytics = get()
            )
        }
        factory<SecretKeyContract.Presenter> {
            SecretKeyPresenter(
                resources = androidContext().resources,
                secretKeyInteractor = get(),
                fileRepository = get()
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
