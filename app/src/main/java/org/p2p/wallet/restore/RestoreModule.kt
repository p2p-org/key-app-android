package org.p2p.wallet.restore

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsContract
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsPresenter
import org.p2p.wallet.restore.ui.seedphrase.SeedPhraseContract
import org.p2p.wallet.restore.ui.seedphrase.SecretKeyPresenter

object RestoreModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SeedPhraseInteractor)
        factory<SeedPhraseContract.Presenter> {
            SecretKeyPresenter(
                seedPhraseInteractor = get(),
            )
        }
        factory<DerivableAccountsContract.Presenter> { (secretKeys: List<SeedPhraseKey>) ->
            DerivableAccountsPresenter(
                secretKeys = secretKeys,
                seedPhraseInteractor = get(),
                usernameInteractor = get(),
                analytics = get()
            )
        }
    }
}
