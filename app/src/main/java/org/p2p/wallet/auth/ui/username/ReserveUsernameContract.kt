package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.json.JSONObject
import java.io.File

interface ReserveUsernameContract {

    interface View : MvpView {
        fun navigateToPinCode()
        fun showUnavailableName(name: String)
        fun showAvailableName(name: String)
        fun getCaptchaResult(params: JSONObject)
        fun successCaptcha()
        fun failCaptcha()
        fun successRegisterName()
        fun failRegisterName()
        fun showFile(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername(username: String)
        fun checkCaptcha()
        fun registerUsername(username: String, result: String?)
        fun openPrivacyPolicy()
        fun openTermsOfUse()
    }
}