package com.p2p.wallet.auth.ui.username

import com.google.gson.Gson
import com.p2p.wallet.auth.interactor.ReservingUsernameInteractor
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

    override fun checkUsername(username: String) {
        checkUsernameJob?.cancel()
        checkUsernameJob = launch {
            try {
                interactor.checkUsername(username)
            } catch (e: HttpException) {
                e.message()
            }
        }
    }

    override fun checkCaptcha() {
        launch {
            try {
                val getCaptchaResponse = interactor.checkCaptcha()
                val gson = Gson()
                gson.toJson(getCaptchaResponse)
                val jsonObject = JSONObject(gson.toJson(getCaptchaResponse))
                view?.getCaptchaResult(jsonObject)
            } catch (e: HttpException) {
                e.message()
            }
        }
    }

    override fun registerUsername(result: String?) {
        launch {
            interactor.registerUsername()
        }
    }
}