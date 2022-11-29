package org.p2p.wallet.settings.ui.recovery.user_seed_phrase

import kotlinx.coroutines.launch
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider

class UserSeedPhrasePresenter(
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<UserSeedPhraseContract.View>(),
    UserSeedPhraseContract.Presenter {

    private val userSeedPhraseWord = mutableListOf<SeedPhraseWord>()

    override fun attach(view: UserSeedPhraseContract.View) {
        super.attach(view)
        launch {
            try {
                userSeedPhraseWord.clear()
                userSeedPhraseWord.addAll(
                    seedPhraseProvider.seedPhrase.map {
                        SeedPhraseWord(
                            text = it,
                            isValid = true,
                            isBlurred = false
                        )
                    }
                )
                view.showSeedPhase(userSeedPhraseWord)
            } catch (e: Throwable) {
                view.showErrorMessage(e)
            }
        }
    }

    override fun onCopyClicked() {
        view?.copyToClipboard(seedPhraseProvider.seedPhrase.joinToString(" "))
    }

    override fun onBlurStateChanged(isBlurred: Boolean) {
        userSeedPhraseWord.forEach { it.isBlurred = isBlurred }
        view?.showSeedPhase(userSeedPhraseWord)
    }
}
