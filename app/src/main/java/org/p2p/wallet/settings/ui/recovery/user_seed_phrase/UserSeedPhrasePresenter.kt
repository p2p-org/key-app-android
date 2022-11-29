package org.p2p.wallet.settings.ui.recovery.user_seed_phrase

import kotlinx.coroutines.launch
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider

class UserSeedPhrasePresenter(
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<UserSeedPhraseContract.View>(),
    UserSeedPhraseContract.Presenter {

    private val userSeedPhrase = mutableListOf<SeedPhraseWord>()

    override fun attach(view: UserSeedPhraseContract.View) {
        super.attach(view)
        launch {
            try {
                userSeedPhrase.clear()
                userSeedPhrase.addAll(
                    seedPhraseProvider.seedPhrase.map {
                        SeedPhraseWord(
                            text = it,
                            isValid = true,
                            isBlurred = false
                        )
                    }
                )
                view.showSeedPhase(userSeedPhrase)
            } catch (e: Throwable) {
                view.showUiKitSnackBar(e.message)
            }
        }
    }

    override fun onCopyClicked() {
        try {
            view?.copyToClipboard(seedPhraseProvider.seedPhrase.joinToString(" "))
        } catch (e: Throwable) {
            view?.showUiKitSnackBar(e.message)
        }
    }

    override fun onBlurStateChanged(isBlurred: Boolean) {
        userSeedPhrase.forEach { it.isBlurred = isBlurred }
        view?.showSeedPhase(userSeedPhrase)
    }
}
