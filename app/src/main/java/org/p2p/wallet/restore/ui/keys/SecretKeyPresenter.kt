package org.p2p.wallet.restore.ui.keys

import android.content.res.Resources
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.model.SeedPhraseResult
import kotlin.properties.Delegates

class SecretKeyPresenter(
    private val resources: Resources,
    private val secretKeyInteractor: SecretKeyInteractor,
    private val fileRepository: FileRepository,
) : BasePresenter<SecretKeyContract.View>(), SecretKeyContract.Presenter {

    companion object {
        private const val SEED_PHRASE_SIZE_SHORT = 12
        private const val SEED_PHRASE_SIZE_LONG = 24
    }

    private var keys: List<SecretKey> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            val size = newValue.size
            val isVisible = size == SEED_PHRASE_SIZE_LONG || size == SEED_PHRASE_SIZE_SHORT
            view?.setButtonEnabled(isVisible)
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

    override fun openTermsOfUse() {
        val inputStream = resources.assets.open("p2p_terms_of_service.pdf")
        val file = fileRepository.savePdf("p2p_terms_of_service", inputStream.readBytes())
        view?.showFile(file)
    }

    override fun openPrivacyPolicy() {
        val inputStream = resources.assets.open("p2p_privacy_policy.pdf")
        val file = fileRepository.savePdf("p2p_privacy_policy", inputStream.readBytes())
        view?.showFile(file)
    }
}
