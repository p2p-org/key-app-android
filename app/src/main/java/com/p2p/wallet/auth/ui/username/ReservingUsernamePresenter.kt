package com.p2p.wallet.auth.ui.username

import android.util.Log
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
    private var owner: String = ""

    override fun checkUsername(username: String) {
        checkUsernameJob?.cancel()
        checkUsernameJob = launch {
            try {
                val usernameCheckResponse = interactor.checkUsername(username)
                view?.showUnavailableName(username, usernameCheckResponse)
            } catch (e: HttpException) {
//                owner = usernameCheckResponse.owner
                Log.i("efefef owner", owner)
                view?.showAvailableName(username)
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

    //    kstep-test-8
    override fun registerUsername(result: String?) {
        val credentials = Gson().fromJson(result, NameRegisterBody.Credentials::class.java)

        Log.i("efefef geeTestValidate", credentials.geeTestValidate)
        Log.i("efefef geeTestSecCode", credentials.geeTestSecCode)
        Log.i("efefef geeTestChallenge", credentials.geeTestChallenge)

        launch {
            try {
                interactor.registerUsername(NameRegisterBody(owner = owner, credentials = credentials))
                view?.successRegisterName()
            } catch (e: HttpException) {
                view?.failRegisterName()
                e.message()
            }
        }
    }
}