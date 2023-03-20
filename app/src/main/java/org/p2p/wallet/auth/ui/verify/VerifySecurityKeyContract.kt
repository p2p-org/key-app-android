package org.p2p.wallet.auth.ui.verify

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

@Deprecated("Old onboarding flow, delete someday")
interface VerifySecurityKeyContract {
    interface View : MvpView {
        fun showKeys(keys: List<SecurityKeyTuple>)
        fun showLoading(isLoading: Boolean)
        fun navigateToReserve()
        fun showKeysDoesNotMatchError()
        fun onCleared()
        fun showEnabled(isEnable: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load(selectedKeys: List<String>, shuffle: Boolean = true)
        fun onKeySelected(keyIndex: Int, selectedKey: String)
        fun validateSecurityKey()
        fun retry()
    }
}
