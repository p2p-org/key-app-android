package org.p2p.wallet.settings.ui.reset.seedphrase

import org.p2p.wallet.auth.analytics.AuthAnalytics
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SeedPhraseInteractor
import org.p2p.uikit.organisms.seedphrase.SeedPhraseWord
import kotlin.properties.Delegates

class ResetSeedPhrasePresenter(
    private val seedPhraseInteractor: SeedPhraseInteractor,
    private val authAnalytics: AuthAnalytics
) : BasePresenter<ResetSeedPhraseContract.View>(), ResetSeedPhraseContract.Presenter {

    companion object {
        private const val SEED_PHRASE_SIZE_SHORT = 12
        private const val SEED_PHRASE_SIZE_LONG = 24
    }

    private var keys: List<SeedPhraseWord> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            val size = newValue.size
            val isVisible = size == SEED_PHRASE_SIZE_LONG || size == SEED_PHRASE_SIZE_SHORT
            view?.setButtonEnabled(isVisible)
        }
    }

    override fun setNewKeys(keys: List<SeedPhraseWord>) {
        val filtered = keys.filter { it.text.isNotEmpty() }
        this.keys = ArrayList(filtered)
    }

    override fun verifySeedPhrase() {
        val validatedKeys = seedPhraseInteractor.verifySeedPhrase(keys)
        val isValid = validatedKeys.none { !it.isValid }

        val resetResult = if (isValid) AuthAnalytics.ResetResult.SUCCESS else AuthAnalytics.ResetResult.ERROR
        authAnalytics.logAuthResetValidated(resetResult)
    }
}
