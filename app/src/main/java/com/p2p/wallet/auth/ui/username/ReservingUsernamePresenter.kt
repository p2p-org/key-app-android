package com.p2p.wallet.auth.ui.username

import com.google.gson.Gson
import com.p2p.wallet.auth.interactor.ReservingUsernameInteractor
import com.p2p.wallet.auth.model.NameRegisterBody
import com.p2p.wallet.common.mvp.BasePresenter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject

class ReservingUsernamePresenter(
    private val interactor: ReservingUsernameInteractor
) :
    BasePresenter<ReservingUsernameContract.View>(),
    ReservingUsernameContract.Presenter {

    private var checkUsernameJob: Job? = null
    private var owner: String? = null

    override fun checkUsername(username: String) {
        checkUsernameJob?.cancel()
        checkUsernameJob = launch {
            try {
                val usernameCheckResponse = interactor.checkUsername(username)
                owner = usernameCheckResponse.owner
                view?.showAvailableName(username, usernameCheckResponse)
            } catch (e: HttpException) {
                view?.showUnavailableName(username)
                e.message()
            }
        }
    }

    override fun checkCaptcha() {
        launch {
            try {
                val getCaptchaResponse = interactor.checkCaptcha()
                view?.getCaptchaResult(JSONObject(Gson().toJson(getCaptchaResponse)))
            } catch (e: HttpException) {
                e.message()
            }
        }
    }

    override fun registerUsername(result: String?) {
        val credentials = Gson().fromJson(result, NameRegisterBody.Credentials::class.java)
        launch {
            try {
                owner?.let {
                    NameRegisterBody(
                        owner = it,
                        credentials = credentials
                    )
                }?.let {
                    interactor.registerUsername(it)
                    view?.finishRegisterName()
                }
            } catch (e: HttpException) {
                e.message()
            }
        }
    }
}