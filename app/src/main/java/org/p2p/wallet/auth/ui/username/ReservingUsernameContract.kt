package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.json.JSONObject

interface ReservingUsernameContract {

    interface View : MvpView {
        fun navigateToPinCode()
        fun showUnavailableName(name: String)
        fun showAvailableName(name: String)
        fun getCaptchaResult(params: JSONObject)
        fun successCaptcha()
        fun failCaptcha()
        fun successRegisterName()
        fun failRegisterName()
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername(username: String)
        fun checkCaptcha()
        fun registerUsername(username: String, result: String?)
    }
}