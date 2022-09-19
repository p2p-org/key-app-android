package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.R
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
        onSeedPhraseUpdated(newValue)
    }

    private fun onSeedPhraseUpdated(newValue: List<SeedPhraseWord>) {
        val newSeedPhraseSize = newValue.size
        val isValid = newValue.all { it.isValid } &&
            (newSeedPhraseSize == SEED_PHRASE_SIZE_LONG || newSeedPhraseSize == SEED_PHRASE_SIZE_SHORT)
        view?.showSeedPhraseValid(isValid)

        val isClearButtonVisible = newValue.isNotEmpty()
        view?.showClearButton(isClearButtonVisible)
    }

    override fun attach(view: SeedPhraseContract.View) {
        super.attach(view)

        if (currentSeedPhrase.isEmpty()) {
            view.addFirstKey(SeedPhraseWord.EMPTY_WORD)
            view.showSeedPhraseValid(isSeedPhraseValid = false)
        } else {
            currentSeedPhrase = currentSeedPhrase.toList()
            view.updateSeedPhrase(currentSeedPhrase)
        }
    }

    override fun setNewSeedPhrase(seedPhrase: List<SeedPhraseWord>) {
        val filteredPhrase = seedPhrase.filter { it.text.isNotEmpty() }
        this.currentSeedPhrase = filteredPhrase.toMutableList()
    }

    override fun verifySeedPhrase() {
        launch {
            when (val result = seedPhraseInteractor.verifySeedPhrase(currentSeedPhrase)) {
                is SeedPhraseInteractor.SeedPhraseVerifyResult.VerifiedSeedPhrase -> {
                    currentSeedPhrase = result.seedPhraseWord
                    if (currentSeedPhrase.all(SeedPhraseWord::isValid)) {
                        view?.navigateToDerievableAccounts(currentSeedPhrase)
                    } else {
                        // warning: updateSeedPhrase causes keyboard to appear, so add a check
                        view?.updateSeedPhrase(currentSeedPhrase)
                        view?.showUiKitSnackBar(messageResId = R.string.seed_phrase_verify_words_failed)
                    }
                }
                is SeedPhraseInteractor.SeedPhraseVerifyResult.VerifyByChecksumFailed -> {
                    view?.showUiKitSnackBar(messageResId = R.string.seed_phrase_verify_checksum_failed)
                }
            }
        }
    }

    override fun requestFocusOnLastWord() {
        view?.showFocusOnLastWord()
    }
}
