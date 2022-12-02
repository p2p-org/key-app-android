package org.p2p.wallet.settings.ui.recovery.user_seed_phrase

import kotlinx.coroutines.launch
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProviderType

class UserSeedPhrasePresenter(
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<UserSeedPhraseContract.View>(),
    UserSeedPhraseContract.Presenter {

    private val seedPhrase = mutableListOf<SeedPhraseWord>()
    private var seedPhraseProviderType: SeedPhraseProviderType? = null

    override fun attach(view: UserSeedPhraseContract.View) {
        super.attach(view)
        launch {
            try {
                seedPhrase.clear()
                seedPhraseProviderType = seedPhraseProvider.getUserSeedPhrase().provider
                seedPhrase.addAll(
                    seedPhraseProvider.getUserSeedPhrase().seedPhrase.map {
                        SeedPhraseWord(
                            text = it,
                            isValid = true,
                            isBlurred = true
                        )
                    }
                )
                view.showSeedPhase(seedPhrase, isEditable = false)
            } catch (e: Throwable) {
                view.showUiKitSnackBar(e.message)
            }
        }
    }

    override fun onCopyClicked() {
        try {
            view?.copyToClipboard(seedPhrase.map { it.text }.joinToString(separator = " "))
        } catch (e: Throwable) {
            view?.showUiKitSnackBar(e.message)
        }
    }

    override fun onBlurStateChanged(isBlurred: Boolean) {
        seedPhrase.forEach { it.isBlurred = isBlurred }
        view?.showSeedPhase(seedPhaseList = seedPhrase, isEditable = false)
    }
}
