package org.p2p.wallet.restore.ui.keys

import android.content.res.Resources
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import org.p2p.wallet.restore.model.SecretKey
import org.p2p.wallet.restore.model.SeedPhraseResult
import kotlin.properties.Delegates

private const val SEED_PHRASE_SIZE_SHORT = 12
private const val SEED_PHRASE_SIZE_LONG = 24

private const val TERMS_OF_SERVICE_FILE_FULL = "p2p_terms_of_service.pdf"
private const val TERMS_OF_SERVICE_FILE_NAME = "p2p_terms_of_service"

private const val PRIVACY_POLICY_FILE_FULL = "p2p_privacy_policy.pdf"
private const val PRIVACY_POLICY_FILE_NAME = "p2p_privacy_policy"

class SecretKeyPresenter(
    private val resources: Resources,
    private val secretKeyInteractor: SecretKeyInteractor,
    private val fileRepository: FileRepository,
) : BasePresenter<SecretKeyContract.View>(), SecretKeyContract.Presenter {

    private var keys: List<SecretKey> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        val newSeedPhraseSize = newValue.size
        val isSeedPhraseValid =
            newSeedPhraseSize == SEED_PHRASE_SIZE_LONG || newSeedPhraseSize == SEED_PHRASE_SIZE_SHORT
        view?.setButtonEnabled(isSeedPhraseValid)
    }

    override fun setNewKeys(keys: List<SecretKey>) {
        val filtered = keys.filter { it.text.isNotEmpty() }
        this.keys = filtered.toMutableList()
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
        val inputStream = resources.assets.open(TERMS_OF_SERVICE_FILE_FULL)
        val file = fileRepository.savePdf(TERMS_OF_SERVICE_FILE_NAME, inputStream.readBytes())
        view?.showFile(file)
    }

    override fun load() {
        if (keys.isEmpty()) {
            view?.addFirstKey(SecretKey())
        } else {
            keys = keys.toList()
        }
    }

    override fun requestFocusOnLastKey() {
        view?.showFocusOnLastKey(keys.size)
    }

    override fun openPrivacyPolicy() {
        val inputStream = resources.assets.open(PRIVACY_POLICY_FILE_FULL)
        val file = fileRepository.savePdf(PRIVACY_POLICY_FILE_NAME, inputStream.readBytes())
        view?.showFile(file)
    }
}
