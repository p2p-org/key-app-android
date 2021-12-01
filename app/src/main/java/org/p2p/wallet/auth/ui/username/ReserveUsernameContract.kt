package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.json.JSONObject
import java.io.File

interface ReserveUsernameContract {

    interface View : MvpView {
        fun navigateToPinCode()
        fun showIdleState()
        fun showUnavailableName(name: String)
        fun showAvailableName(name: String)
        fun showCaptcha(params: JSONObject)
        fun showCaptchaSucceeded()
        fun showCaptchaFailed()
        fun showSuccess()
        fun showLoading(isLoading: Boolean)
        fun showUsernameLoading(isLoading: Boolean)
        fun showFile(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername(username: String)
        fun checkCaptcha()
        fun registerUsername(username: String, result: String)
        fun openPrivacyPolicy()
        fun openTermsOfUse()
    }
}