package org.p2p.wallet.restore.ui.keys

import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.p2p.wallet.restore.model.SeedPhraseResult
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class SecretKeyPresenter(
    private val secretKeyInteractor: SecretKeyInteractor
) : BasePresenter<SecretKeyContract.View>(), SecretKeyContract.Presenter {

    companion object {
        private const val SEED_PHRASE_SIZE_SHORT = 12
        private const val SEED_PHRASE_SIZE_LONG = 24
    }

    private var keys: List<SecretKey> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            val size = newValue.size
            val isVisible = size == SEED_PHRASE_SIZE_LONG || size == SEED_PHRASE_SIZE_SHORT
            view?.showActionButtons(isVisible)
        }
    }

    override fun setNewKeys(keys: List<SecretKey>) {
        val filtered = keys.filter { it.text.isNotEmpty() }
        this.keys = ArrayList(filtered)
    }

    override fun verifySeedPhrase() {
        launch {
            when (val data = secretKeyInteractor.verifySeedPhrase(keys)) {
                is SeedPhraseResult.Success -> view?.showSuccess(data.secretKeys)
                is SeedPhraseResult.Error -> view?.showError(data.message)
            }
        }
    }
}