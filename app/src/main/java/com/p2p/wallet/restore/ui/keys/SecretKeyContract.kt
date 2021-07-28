package com.p2p.wallet.restore.ui.keys

import androidx.annotation.StringRes
import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView
import com.p2p.wallet.restore.model.SecretKey

interface SecretKeyContract {

    interface View : MvpView {
        fun showSuccess(secretKeys: List<SecretKey>)
        fun showError(@StringRes messageRes: Int)
        fun showActionButtons(isVisible: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun setNewKeys(keys: List<SecretKey>)
        fun verifySeedPhrase()
    }
}