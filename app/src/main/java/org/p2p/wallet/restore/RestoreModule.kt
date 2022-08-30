package org.p2p.wallet.restore

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.di.InjectionModule
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsContract
import org.p2p.wallet.restore.ui.derivable.DerivableAccountsPresenter
import org.p2p.wallet.restore.ui.seedphrase.SeedPhraseContract
import org.p2p.wallet.restore.ui.seedphrase.SeedPhrasePresenter

object RestoreModule : InjectionModule {

    override fun create() = module {
        factoryOf(::SeedPhraseInteractor)
        factoryOf(::SeedPhrasePresenter) bind SeedPhraseContract.Presenter::class
        factory<DerivableAccountsContract.Presenter> { (secretKeys: List<SeedPhraseWord>) ->
            DerivableAccountsPresenter(
                secretKeys = secretKeys,
                seedPhraseInteractor = get(),
                usernameInteractor = get(),
                analytics = get()
            )
        }
    }
}
