package org.p2p.wallet.restore.ui.seedphrase

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.uikit.organisms.seedphrase.SeedPhraseKey
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

class SecretKeyPresenter(
    private val seedPhraseInteractor: SeedPhraseInteractor,
) : BasePresenter<SeedPhraseContract.View>(), SeedPhraseContract.Presenter {

    private var keys: List<SeedPhraseKey> by Delegates.observable(emptyList()) { _, _, newValue ->
        val newSeedPhraseSize = newValue.size
        val isSeedPhraseValid =
            newSeedPhraseSize == SEED_PHRASE_SIZE_LONG || newSeedPhraseSize == SEED_PHRASE_SIZE_SHORT
        view?.showSeedPhraseValid(isSeedPhraseValid)

        val isClearButtonVisible = newValue.isNotEmpty()
        view?.showClearButton(isClearButtonVisible)
    }

    override fun setNewKeys(keys: List<SeedPhraseKey>) {
        val filtered = keys.filter { it.text.isNotEmpty() }
        this.keys = filtered.toMutableList()
    }

    override fun verifySeedPhrase() {
        launch {
            val seedPhrase = seedPhraseInteractor.verifySeedPhrase(keys).also { keys = it }
            view?.updateSeedPhrase(seedPhrase)
        }
    }

    override fun load() {
        if (keys.isEmpty()) {
            view?.addFirstKey(SeedPhraseKey())
        } else {
            keys = keys.toList()
        }
    }

    override fun requestFocusOnLastKey() {
        view?.showFocusOnLastKey(keys.size)
    }
}
