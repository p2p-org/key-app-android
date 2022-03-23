package org.p2p.wallet.auth.ui.username

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.json.JSONObject

interface ReserveUsernameContract {

    interface View : MvpView {
        fun navigateToPinCode()
        fun navigateToUsername()
        fun showIdleState()
        fun showUnavailableName(name: String)
        fun showAvailableName(name: String)
        fun showCaptcha(params: JSONObject)
        fun showCaptchaSucceeded()
        fun showCaptchaFailed()
        fun showSuccess()
        fun showLoading(isLoading: Boolean)
        fun showUsernameLoading(isLoading: Boolean)
        fun finishNavigation()
        fun showCustomFlow()
    }

    interface Presenter : MvpPresenter<View> {
        fun checkUsername(username: String)
        fun checkCaptcha()
        fun registerUsername(username: String, result: String)
        fun onSkipClicked()
        fun save()
    }
}
