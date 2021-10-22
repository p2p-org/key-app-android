package com.p2p.wallet.auth.ui.username

import com.google.gson.Gson
import com.p2p.wallet.auth.interactor.UsernameInteractor
import com.p2p.wallet.common.mvp.BasePresenter
import com.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class ReservingUsernamePresenter(
    private val interactor: UsernameInteractor,
    private val tokenProvider: TokenKeyProvider,
) :
    BasePresenter<ReservingUsernameContract.View>(),
    ReservingUsernameContract.Presenter {

    private var checkUsernameJob: Job? = null
    private var nextCheckAvailable: Boolean = true

    override fun checkUsername(username: String) {
        if (nextCheckAvailable) {
            nextCheckAvailable = false
            checkUsernameJob?.cancel()
            checkUsernameJob = launch {
                try {
                    interactor.checkUsername(username)
                    view?.showUnavailableName(username)
                } catch (e: HttpException) {
                    view?.showAvailableName(username)
                }
                delay(300)
                nextCheckAvailable = true
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
}