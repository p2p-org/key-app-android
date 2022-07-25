package org.p2p.wallet.auth.ui.phone.countrypicker

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CountryPickerContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
