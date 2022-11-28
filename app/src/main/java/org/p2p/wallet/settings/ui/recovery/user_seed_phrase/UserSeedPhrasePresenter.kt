package org.p2p.wallet.settings.ui.recovery.user_seed_phrase

import kotlinx.coroutines.launch
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import timber.log.Timber

class UserSeedPhrasePresenter(
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<UserSeedPhraseContract.View>(),
    UserSeedPhraseContract.Presenter {

    override fun attach(view: UserSeedPhraseContract.View) {
        super.attach(view)
        launch {
            try {
                Timber.tag("____________PROVIDER").d(seedPhraseProvider.toString())
                val userSeedPhase = seedPhraseProvider.seedPhrase.map {
                    SeedPhraseWord(
                        text = it,
                        isValid = true
                    )
                }
                view.showSeedPhase(userSeedPhase)
            } catch (e: Throwable) {
                view.showErrorMessage(e)
            }
        }
    }
}
