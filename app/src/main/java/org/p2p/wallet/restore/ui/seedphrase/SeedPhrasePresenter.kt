package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.launch
import org.p2p.wallet.restore.model.SeedPhraseVerifyResult

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
    private val seedPhraseProvider: SeedPhraseProvider
) : BasePresenter<SeedPhraseContract.View>(), SeedPhraseContract.Presenter {

    private var currentSeedPhrase: List<SeedPhraseWord> by observable(emptyList()) { _, _, newValue ->
        onSeedPhraseUpdated(newValue)
    }

    private fun onSeedPhraseUpdated(newValue: List<SeedPhraseWord>) {
        val newSeedPhraseSize = newValue.size
        val isValid = newValue.all { it.isValid } &&
            (newSeedPhraseSize == SEED_PHRASE_SIZE_LONG || newSeedPhraseSize == SEED_PHRASE_SIZE_SHORT)
        view?.showSeedPhraseValid(isValid)

        val isClearButtonVisible = newValue.isNotEmpty()
        view?.setClearButtonVisible(isClearButtonVisible)
    }

    override fun attach(view: SeedPhraseContract.View) {
        super.attach(view)

        // clear seedPhrase on each attach for security reasons
        currentSeedPhrase = emptyList()
        view.addFirstSeedPhraseWord()
    }

    override fun setNewSeedPhrase(seedPhrase: List<SeedPhraseWord>) {
        val filteredPhrase = seedPhrase.filter { it.text.isNotEmpty() }
        this.currentSeedPhrase = filteredPhrase.toMutableList()
    }

    override fun verifySeedPhrase() {
        launch {
            when (val result = seedPhraseInteractor.verifySeedPhrase(currentSeedPhrase)) {
                is SeedPhraseVerifyResult.Verified -> {
                    currentSeedPhrase = result.seedPhrase
                    seedPhraseProvider.setUserSeedPhrase(
                        words = result.getKeys(),
                        provider = SeedPhraseSource.MANUAL
                    )
                }
                is SeedPhraseVerifyResult.Invalid -> {
                    // warning: updateSeedPhrase causes keyboard to appear, so add a check
                    currentSeedPhrase = result.seedPhraseWord
                    view?.updateSeedPhraseView(currentSeedPhrase)
                    view?.showUiKitSnackBar(messageResId = R.string.seed_phrase_verify_words_failed)
                }
                is SeedPhraseVerifyResult.VerificationFailed -> {
                    view?.showUiKitSnackBar(messageResId = R.string.seed_phrase_verify_checksum_failed)
                }
            }
        }
    }

    override fun requestFocusOnLastWord() {
        view?.showFocusOnLastWord()
    }
}
