package org.p2p.wallet.auth.ui.username

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.repository.FileRepository
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import retrofit2.HttpException

class ReserveUsernamePresenter(
    private val context: Context,
    private val interactor: UsernameInteractor,
    private val tokenProvider: TokenKeyProvider,
    private val fileRepository: FileRepository
) : BasePresenter<ReserveUsernameContract.View>(),
    ReserveUsernameContract.Presenter {

    private var checkUsernameJob: Job? = null

    override fun checkUsername(username: String) {
        checkUsernameJob?.cancel()
        checkUsernameJob = launch {
            try {
                /*
                * We should check the availability of the latest entered value by the user
                * therefore we cancel old request if new value is entered and waiting for the latest response only
                * */
                delay(300)
                interactor.checkUsername(username)
                view?.showUnavailableName(username)
            } catch (e: HttpException) {
                view?.showAvailableName(username)
            }
        }
    }

    override fun checkCaptcha() {
        launch {
            try {
                val getCaptchaResponse = interactor.checkCaptcha()
                view?.getCaptchaResult(JSONObject(Gson().toJson(getCaptchaResponse)))
            } catch (e: HttpException) {
                view?.failCaptcha()
                view?.showErrorMessage(e)
            }
        }
    }

    override fun registerUsername(username: String, result: String?) {
        launch {
            try {
                interactor.registerUsername(username, result)
                interactor.lookupUsername(tokenProvider.publicKey)
                view?.successRegisterName()
            } catch (e: HttpException) {
                view?.failRegisterName()
                view?.showErrorMessage(e)
            }
        }
    }

    override fun openTermsOfUse() {
        val inputStream = context.assets.open("p2p_terms_of_service.pdf")
        val file = fileRepository.savePdf("p2p_terms_of_service", inputStream.readBytes())
        view?.showFile(file)
    }

    override fun openPrivacyPolicy() {
        val inputStream = context.assets.open("p2p_privacy_policy.pdf")
        val file = fileRepository.savePdf("p2p_privacy_policy", inputStream.readBytes())
        view?.showFile(file)
    }
}