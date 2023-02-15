package org.p2p.wallet.auth.ui.security

import android.graphics.Bitmap
import java.io.File
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

@Deprecated("Old onboarding flow, delete someday")
interface SecurityKeyContract {

    interface View : MvpView {
        fun showKeys(keys: List<String>)
        fun copyToClipboard(keys: List<String>)
        fun showLoading(isLoading: Boolean)
        fun navigateToVerify(keys: List<String>)
        fun captureKeys()
        fun showFile(file: File)
        fun shareScreenShot(file: File)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadKeys()
        fun copyKeys()
        fun cacheKeys()
        fun saveKeys()
        fun openPrivacyPolicy()
        fun openTermsOfUse()
        fun createScreenShootFile(bitmap: Bitmap)
    }
}
