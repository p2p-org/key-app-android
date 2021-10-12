package com.p2p.wallet.auth.ui.username

import com.p2p.wallet.auth.api.UsernameCheckResponse
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import org.json.JSONObject

interface ReservingUsernameContract {

    interface View : MvpView {
        fun navigateToPinCode()
        fun getCaptchaResult(params: JSONObject)
        fun showUnavailableName(name: String, usernameCheckResponse: UsernameCheckResponse)
        fun showAvailableName(name: String)
        fun successRegisterName()
        fun failRegisterName()
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername(username: String)
        fun checkCaptcha()
        fun registerUsername(result: String?)
    }
}