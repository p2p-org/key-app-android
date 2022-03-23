package org.p2p.wallet.auth.ui.security

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.analytics.OnBoardingAnalytics
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.restore.interactor.SecretKeyInteractor
import kotlin.properties.Delegates

class SecurityKeyPresenter(
    private val context: Context,
    private val secretKeyInteractor: SecretKeyInteractor,
    private val fileRepository: FileRepository,
    private val onBoardingAnalytics: OnBoardingAnalytics
) : BasePresenter<SecurityKeyContract.View>(), SecurityKeyContract.Presenter {

    private var keys: List<String> by Delegates.observable(emptyList()) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            view?.showKeys(newValue)
        }
    }

    init {
        loadKeys()
    }

    override fun attach(view: SecurityKeyContract.View) {
        super.attach(view)
        view.showKeys(keys)
    }

    override fun loadKeys() {
        launch {
            keys = secretKeyInteractor.generateSecretKeys()
            onBoardingAnalytics.logBackingUpRenew()
        }
    }

    override fun copyKeys() {
        view?.copyToClipboard(keys)
        onBoardingAnalytics.logBackingUpCopying()
    }

    override fun cacheKeys() {
        launch {
            view?.showLoading(true)
            view?.navigateToVerify(keys)
            view?.showLoading(false)
        }
    }

    override fun saveKeys() {
        view?.captureKeys()
        onBoardingAnalytics.logBackingUpSaving()
    }

    override fun openTermsOfUse() {
        val inputStream = context.assets.open("p2p_terms_of_service.pdf")
        val file = fileRepository.savePdf("p2p_terms_of_service", inputStream.readBytes())
        view?.showFile(file)
    }

    override fun createScreenShootFile(bitmap: Bitmap) {
        try {
            val file = fileRepository.saveBitmapAsFile(bitmap) ?: return
            view?.shareScreenShot(file)
        } catch (e: Exception) {
            view?.showErrorMessage(e)
        }
    }

    override fun openPrivacyPolicy() {
        val inputStream = context.assets.open("p2p_privacy_policy.pdf")
        val file = fileRepository.savePdf("p2p_privacy_policy", inputStream.readBytes())
        view?.showFile(file)
    }
}
