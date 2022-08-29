package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import kotlin.properties.Delegates
import kotlinx.coroutines.launch

private const val SEED_PHRASE_SIZE_SHORT = 12
private const val SEED_PHRASE_SIZE_LONG = 24

// don't delete
// we'll need it in another screens
private const val TERMS_OF_SERVICE_FILE_FULL = "p2p_terms_of_service.pdf"
private const val TERMS_OF_SERVICE_FILE_NAME = "p2p_terms_of_service"

private const val PRIVACY_POLICY_FILE_FULL = "p2p_privacy_policy.pdf"
private const val PRIVACY_POLICY_FILE_NAME = "p2p_privacy_policy"

class SeedPhrasePresenter(
    private val seedPhraseInteractor: SeedPhraseInteractor,
) : BasePresenter<SeedPhraseContract.View>(), SeedPhraseContract.Presenter {

    private var currentSeedPhrase: List<SeedPhraseWord> by Delegates.observable(emptyList()) { _, _, newValue ->
        val newSeedPhraseSize = newValue.size
        val isSeedPhraseValid =
            newSeedPhraseSize == SEED_PHRASE_SIZE_LONG || newSeedPhraseSize == SEED_PHRASE_SIZE_SHORT
        view?.showSeedPhraseValid(isSeedPhraseValid)

        val isClearButtonVisible = newValue.isNotEmpty()
        view?.showClearButton(isClearButtonVisible)
    }

    override fun setNewSeedPhrase(seedPhrase: List<SeedPhraseWord>) {
        val filteredPhrase = seedPhrase.filter { it.text.isNotEmpty() }
        this.currentSeedPhrase = filteredPhrase.toMutableList()
    }

    override fun verifySeedPhrase() {
        launch {
            currentSeedPhrase = seedPhraseInteractor.verifySeedPhrase(currentSeedPhrase)
            view?.updateSeedPhrase(currentSeedPhrase)

            if (currentSeedPhrase.all(SeedPhraseWord::isValid)) {
                view?.navigateToDerievableAccounts(currentSeedPhrase)
            }
        }
    }

    override fun load() {
        if (currentSeedPhrase.isEmpty()) {
            view?.addFirstKey(SeedPhraseWord.EMPTY_WORD)
        } else {
            currentSeedPhrase = currentSeedPhrase.toList()
        }
    }

    override fun requestFocusOnLastWord() {
        view?.showFocusOnLastWord(currentSeedPhrase.size)
    }
}
