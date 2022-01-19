package org.p2p.wallet.auth.ui.verify

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface VerifySecurityKeyContract {
    interface View : MvpView {
        fun showKeys(keys: List<SecurityKeyTuple>)
        fun showLoading(isLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun load(selectedKeys: List<String>)
        fun onKeySelected(keyIndex: Int, selectedKey: String)
        fun validate()
    }
}